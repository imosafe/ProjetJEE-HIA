package fr.cytech.pau.hia_jee.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import fr.cytech.pau.hia_jee.model.Match;
import fr.cytech.pau.hia_jee.model.StatusTournament;
import fr.cytech.pau.hia_jee.model.Team;
import fr.cytech.pau.hia_jee.model.Tournament;
import fr.cytech.pau.hia_jee.repository.MatchRepository;
import fr.cytech.pau.hia_jee.repository.TournamentRepository;
import jakarta.transaction.Transactional;

@Service
//@RequiredArgsConstructor // 1. Génère le constructeur pour l'injection des Repos
public class TournamentService {

    private final TournamentRepository tRepo;
    private final MatchRepository mRepo;

    public TournamentService(TournamentRepository tRepo, MatchRepository mRepo) {
        this.tRepo = tRepo;
        this.mRepo = mRepo;
    }

    @Transactional
    public void generateBracket(Long tournamentId) {
        Tournament tournament = tRepo.findById(tournamentId)
            .orElseThrow(() -> new RuntimeException("Tournoi introuvable"));

        // Nettoyage préalable (si on régénère)
        if (tournament.getMatches() != null) {
            mRepo.deleteAll(tournament.getMatches());
            tournament.getMatches().clear();
        }

        List<Team> teams = tournament.getTeams();
        int teamCount = teams.size();
        if (teamCount < 2) throw new RuntimeException("Il faut au moins 2 équipes !");

        // Calcul taille bracket (puissance de 2)
        int bracketSize = 1;
        while (bracketSize < teamCount) bracketSize *= 2;

        Collections.shuffle(teams);

        List<Match> currentRoundMatches = new ArrayList<>();
        int teamIndex = 0;

        // --- ROUND 1 ---
        for (int i = 0; i < bracketSize / 2; i++) {
            Match match = new Match();
            match.setTournament(tournament);
            match.setRound(1); // Important pour l'affichage

            // Slot A
            if (teamIndex < teams.size()) {
                match.setTeamA(teams.get(teamIndex++));
            }
            
            // Slot B (ou Bye)
            if (teamIndex < teams.size()) {
                match.setTeamB(teams.get(teamIndex++));
                match.setScoreA(0);
                match.setScoreB(0);
            } else {
                // BYE DETECTÉ
                match.setTeamB(null);
                match.setScoreA(1);
                match.setScoreB(0);
                match.setWinner(match.getTeamA()); // Victoire immédiate
            }
            
            match = mRepo.save(match);
            currentRoundMatches.add(match);
        }

        // --- ROUNDS SUIVANTS (Construction de l'arbre) ---
        int roundNumber = 2;
        
        while (currentRoundMatches.size() > 1) {
            List<Match> nextRoundMatches = new ArrayList<>();
            
            for (int i = 0; i < currentRoundMatches.size(); i += 2) {
                Match match1 = currentRoundMatches.get(i);
                Match match2 = currentRoundMatches.get(i + 1);

                // Création du match parent
                Match nextMatch = new Match();
                nextMatch.setTournament(tournament);
                nextMatch.setRound(roundNumber);
                nextMatch.setScoreA(0);
                nextMatch.setScoreB(0);
                
                // --- CORRECTION MAJEURE ICI : PROPAGATION DES BYES ---
                // Si match1 a déjà un vainqueur (cas du Bye), on le fait avancer tout de suite
                if (match1.getWinner() != null) {
                    nextMatch.setTeamA(match1.getWinner());
                }
                
                // Idem pour match2
                if (match2.getWinner() != null) {
                    nextMatch.setTeamB(match2.getWinner());
                }
                // -----------------------------------------------------

                nextMatch = mRepo.save(nextMatch);

                // Liaison enfants -> parent
                match1.setNextMatch(nextMatch);
                match2.setNextMatch(nextMatch);
                mRepo.save(match1);
                mRepo.save(match2);

                nextRoundMatches.add(nextMatch);
            }
            
            currentRoundMatches = nextRoundMatches;
            roundNumber++;
        }
        
        tournament.setStatus(StatusTournament.EN_COURS);
        tRepo.save(tournament);
    }

    public void save(Tournament tournament) {
        tRepo.save(tournament);
    }

    public List<Tournament> findAll() {
        return tRepo.findAll();
    }

    public java.util.Optional<Tournament> findById(Long id) {
        return tRepo.findById(id);
    }
    @Transactional
    public void enterScore(Long matchId, int scoreA, int scoreB) {
        // 1. Récupération du match
        Match match = mRepo.findById(matchId)
            .orElseThrow(() -> new RuntimeException("Match introuvable"));

        // 2. Vérifications de sécurité
        if (match.getTeamA() == null || match.getTeamB() == null) {
            throw new RuntimeException("Le match n'est pas prêt (il manque une équipe).");
        }
        
        if (scoreA == scoreB) {
            throw new RuntimeException("Match nul interdit dans un arbre ! Il faut un vainqueur.");
        }

        // 3. Enregistrement des scores
        match.setScoreA(scoreA);
        match.setScoreB(scoreB);

        // 4. Désignation du vainqueur
        Team winner = (scoreA > scoreB) ? match.getTeamA() : match.getTeamB();
        match.setWinner(winner);
        
        mRepo.save(match); // On sauvegarde l'état actuel

        // 5. PROPAGATION (Faire monter le vainqueur)
        Match nextMatch = match.getNextMatch();

        if (nextMatch != null) {
            // Cas : Il y a un match suivant (on est en 8ème, quart, demi...)
            
            // Logique : On remplit le premier slot vide trouvé
            // (Ou on met à jour si le vainqueur y était déjà, cas de correction de score)
            
            if (nextMatch.getTeamA() == null || nextMatch.getTeamA().equals(winner) || isTeamFromThisMatch(nextMatch.getTeamA(), match)) {
                // Si la place A est vide OU si c'est déjà nous (update) -> On prend la place A
                nextMatch.setTeamA(winner);
            } else {
                // Sinon, on prend la place B
                nextMatch.setTeamB(winner);
            }
            
            mRepo.save(nextMatch);
            
        } else {
            // Cas : Pas de match suivant = C'était la FINALE !
            Tournament tournament = match.getTournament();
            tournament.setStatus(StatusTournament.TERMINE);
            // On pourrait aussi ajouter un champ 'winner' dans l'entité Tournament ici
            tRepo.save(tournament);
        }
    }

    // Petite méthode utilitaire pour éviter les bugs si on modifie un score
    // Elle vérifie si l'équipe actuellement dans le nextMatch venait bien de ce match-là
    private boolean isTeamFromThisMatch(Team teamInNext, Match currentMatch) {
        if (teamInNext == null) return false;
        // Si l'équipe dans le match suivant est l'une des deux équipes du match actuel
        return teamInNext.equals(currentMatch.getTeamA()) || teamInNext.equals(currentMatch.getTeamB());
    }
}