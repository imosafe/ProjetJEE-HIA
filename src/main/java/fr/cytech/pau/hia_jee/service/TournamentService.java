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

/**
 * Service gérant la mécanique sportive des tournois.
 */
@Service
public class TournamentService {

    private final TournamentRepository tRepo;
    private final MatchRepository mRepo;

    public TournamentService(TournamentRepository tRepo, MatchRepository mRepo) {
        this.tRepo = tRepo;
        this.mRepo = mRepo;
    }

    // ============================================================
    // GÉNÉRATION DE L'ARBRE (BRACKET)
    // ============================================================

    /**
     * Génère l'arbre des matchs pour un tournoi donné.
     * Algorithme basé sur les puissances de 2 (2, 4, 8, 16, 32...).
     * Gère les "Byes" (qualification automatique) si le nombre d'équipes n'est pas une puissance de 2.
     */
    @Transactional
    public void generateBracket(Long tournamentId) {
        Tournament tournament = tRepo.findById(tournamentId)
            .orElseThrow(() -> new RuntimeException("Tournoi introuvable"));

        // 1. Nettoyage préalable
        // Si l'admin clique 2 fois sur "Générer", on supprime l'ancien arbre pour éviter les doublons.
        if (tournament.getMatches() != null) {
            mRepo.deleteAll(tournament.getMatches());
            tournament.getMatches().clear();
        }

        List<Team> teams = tournament.getTeams();
        int teamCount = teams.size();
        if (teamCount < 2) throw new RuntimeException("Il faut au moins 2 équipes !");

        // 2. Calcul de la taille du bracket (Puissance de 2 supérieure)
        // Ex: 5 équipes -> bracketSize = 8. (Il y aura 3 "Byes").
        // Ex: 8 équipes -> bracketSize = 8.
        int bracketSize = 1;
        while (bracketSize < teamCount) bracketSize *= 2;

        // Mélange aléatoire des équipes (Seed random)
        Collections.shuffle(teams);

        List<Match> currentRoundMatches = new ArrayList<>();
        int teamIndex = 0;

        // --- 3. GÉNÉRATION DU ROUND 1 (Le bas de l'arbre) ---
        // On crée autant de matchs que bracketSize / 2.
        for (int i = 0; i < bracketSize / 2; i++) {
            Match match = new Match();
            match.setTournament(tournament);
            match.setRound(1); 

            // Remplissage Slot A
            if (teamIndex < teams.size()) {
                match.setTeamA(teams.get(teamIndex++));
            }
            
            // Remplissage Slot B
            if (teamIndex < teams.size()) {
                match.setTeamB(teams.get(teamIndex++));
                // Initialisation des scores à 0 pour l'affichage
                match.setScoreA(0);
                match.setScoreB(0);
            } else {
                // --- GESTION DU BYE ---
                // Si plus d'équipe dispo pour le slot B, l'équipe A gagne automatiquement.
                match.setTeamB(null);
                match.setScoreA(1); // Score arbitraire de victoire
                match.setScoreB(0);
                match.setWinner(match.getTeamA()); // Victoire immédiate !
            }
            
            match = mRepo.save(match);
            currentRoundMatches.add(match);
        }

        // --- 4. CONSTRUCTION DES ROUNDS SUIVANTS (L'arbre monte) ---
        int roundNumber = 2;
        
        // Tant qu'il reste plus d'un match dans le round actuel (donc pas encore la finale)
        while (currentRoundMatches.size() > 1) {
            List<Match> nextRoundMatches = new ArrayList<>();
            
            // On prend les matchs 2 par 2 (Match 1 vs Match 2, Match 3 vs Match 4...)
            for (int i = 0; i < currentRoundMatches.size(); i += 2) {
                Match match1 = currentRoundMatches.get(i);
                Match match2 = currentRoundMatches.get(i + 1);

                // Création du match parent (le tour suivant)
                Match nextMatch = new Match();
                nextMatch.setTournament(tournament);
                nextMatch.setRound(roundNumber);
                nextMatch.setScoreA(0);
                nextMatch.setScoreB(0);
                
                // --- PROPAGATION IMMÉDIATE DES BYES ---
                // C'est crucial : Si au Round 1, une équipe a gagné par "Bye", 
                // elle doit apparaître IMMÉDIATEMENT au Round 2.
                // Sinon, le Round 2 afficherait des matchs vides "TBD vs TBD".
                if (match1.getWinner() != null) {
                    nextMatch.setTeamA(match1.getWinner());
                }
                
                if (match2.getWinner() != null) {
                    nextMatch.setTeamB(match2.getWinner());
                }
                
                nextMatch = mRepo.save(nextMatch);

                // --- LIAISON ENFANTS -> PARENT ---
                // C'est ce qui crée la structure de graphe
                match1.setNextMatch(nextMatch);
                match2.setNextMatch(nextMatch);
                mRepo.save(match1);
                mRepo.save(match2);

                nextRoundMatches.add(nextMatch);
            }
            
            // On passe à l'étage supérieur
            currentRoundMatches = nextRoundMatches;
            roundNumber++;
        }
        
        // Le tournoi est lancé !
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

    // ============================================================
    // SAISIE DE SCORE ET AVANCEMENT
    // ============================================================

    /**
     * Enregistre le score d'un match et fait avancer le vainqueur.
     */
    @Transactional
    public void enterScore(Long matchId, int scoreA, int scoreB) {
        // 1. Récupération du match
        Match match = mRepo.findById(matchId)
            .orElseThrow(() -> new RuntimeException("Match introuvable"));

        // 2. Sécurités
        if (match.getTeamA() == null || match.getTeamB() == null) {
            throw new RuntimeException("Le match n'est pas prêt (il manque une équipe).");
        }
        
        if (scoreA == scoreB) {
            throw new RuntimeException("Match nul interdit dans un arbre ! Il faut un vainqueur.");
        }

        // 3. Mise à jour des scores
        match.setScoreA(scoreA);
        match.setScoreB(scoreB);

        // 4. Calcul du vainqueur
        Team winner = (scoreA > scoreB) ? match.getTeamA() : match.getTeamB();
        match.setWinner(winner);
        
        mRepo.save(match); 

        // 5. PROPAGATION (Le vainqueur monte au tour suivant)
        Match nextMatch = match.getNextMatch();

        if (nextMatch != null) {
            // Cas classique : Ce n'est pas la finale.
            
            // On doit savoir : est-ce que ce vainqueur va en position A ou en position B du match suivant ?
            // Logique : 
            // - Si la place A est vide -> On se met en A.
            // - Si la place A est occupée par NOUS-MÊME (cas où l'admin corrige un score saisi par erreur) -> On reste en A.
            // - Sinon -> On va en B.
            
            if (nextMatch.getTeamA() == null || nextMatch.getTeamA().equals(winner) || isTeamFromThisMatch(nextMatch.getTeamA(), match)) {
                nextMatch.setTeamA(winner);
            } else {
                nextMatch.setTeamB(winner);
            }
            
            mRepo.save(nextMatch);
            
        } else {
            // Cas final : Pas de match suivant, donc le tournoi est fini.
            Tournament tournament = match.getTournament();
            tournament.setStatus(StatusTournament.TERMINE);
            tRepo.save(tournament);
        }
    }

    /**
     * Méthode utilitaire pour vérifier l'origine d'une équipe.
     * Sert à éviter qu'une équipe change de place (A vers B) si on modifie un score.
     */
    private boolean isTeamFromThisMatch(Team teamInNext, Match currentMatch) {
        if (teamInNext == null) return false;
        // Vérifie si l'équipe présente dans le match suivant venait bien de ce match-ci
        return teamInNext.equals(currentMatch.getTeamA()) || teamInNext.equals(currentMatch.getTeamB());
    }
}