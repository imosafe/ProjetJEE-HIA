package fr.cytech.pau.hia_jee.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import fr.cytech.pau.hia_jee.model.Match;
import fr.cytech.pau.hia_jee.model.Team;
import fr.cytech.pau.hia_jee.model.Tournament;
import fr.cytech.pau.hia_jee.repository.MatchRepository;
import fr.cytech.pau.hia_jee.repository.TournamentRepository;

@Service
//@RequiredArgsConstructor // 1. Génère le constructeur pour l'injection des Repos
public class TournamentService {

    private final TournamentRepository tRepo;
    private final MatchRepository mRepo;

    public TournamentService(TournamentRepository tRepo, MatchRepository mRepo) {
        this.tRepo = tRepo;
        this.mRepo = mRepo;
    }

    public void generateBracket(Long tournamentId) {
        // --- ÉTAPE 1 : Préparation des données ---
        Tournament tournament = tRepo.findById(tournamentId)
            .orElseThrow(() -> new RuntimeException("Tournoi introuvable"));
        
        // Empêcher de re-générer l'arbre si déjà fait
        if (tournament.getMatches() != null && !tournament.getMatches().isEmpty()) {
             throw new RuntimeException("L'arbre de ce tournoi est déjà généré.");
        }

        List<Team> teams = tournament.getTeams();
        int teamCount = teams.size();
        
        // Calcul de la puissance de 2 supérieure (ex: 5 équipes -> taille 8)
        int bracketSize = 1;
        while (bracketSize < teamCount) {
            bracketSize *= 2;
        }

        // Mélange aléatoire pour le tirage au sort
        Collections.shuffle(teams);

        // --- ÉTAPE 2 : Génération du Round 1 (Votre logique + Byes) ---
        List<Match> currentRoundMatches = new ArrayList<>();
        int teamIndex = 0;

        for (int i = 0; i < bracketSize / 2; i++) {
            Match match = new Match();
            match.setTournament(tournament);
            
            // Slot A
            if (teamIndex < teams.size()) {
                match.setTeamA(teams.get(teamIndex));
                teamIndex++;
            }
            
            // Slot B (Ou Bye)
            if (teamIndex < teams.size()) {
                match.setTeamB(teams.get(teamIndex));
                teamIndex++;
                // Match classique : pas de vainqueur, scores à 0
                match.setScoreA(0);
                match.setScoreB(0);
            } else {
                // BYE : Pas d'adversaire
                match.setTeamB(null);
                // Victoire technique pour A
                match.setScoreA(1); // Score symbolique
                match.setScoreB(0);
                match.setWinner(match.getTeamA()); // Vainqueur défini immédiatement !
            }
            
            // On sauvegarde le match et on l'ajoute à la liste du round actuel
            match = mRepo.save(match); 
            currentRoundMatches.add(match);
        }

        // --- ÉTAPE 3 : Construction de l'Arbre (Rounds suivants) ---
        // C'est ici qu'on crée la relation "Match Suivant" 
        
        // Tant qu'il reste plus d'un match dans le round actuel, on doit créer le round suivant
        while (currentRoundMatches.size() > 1) {
            List<Match> nextRoundMatches = new ArrayList<>();
            
            // On prend les matchs par paire (2 matchs du round 1 se rejoignent en 1 match du round 2)
            for (int i = 0; i < currentRoundMatches.size(); i += 2) {
                Match match1 = currentRoundMatches.get(i);
                Match match2 = currentRoundMatches.get(i + 1);

                // Création du match "Parent" (Match Suivant)
                Match nextMatch = new Match();
                nextMatch.setTournament(tournament);
                nextMatch.setScoreA(0);
                nextMatch.setScoreB(0);
                // Les équipes sont nulles pour l'instant (elles arriveront plus tard via progression)
                
                // Si des vainqueurs existent déjà dans les matchs précédents (cas des BYES),
                // on peut potentiellement déjà remplir ce match, mais restons simples pour l'instant.
                
                nextMatch = mRepo.save(nextMatch); // Sauvegarder d'abord pour avoir un ID

                // Lier les enfants au parent
                match1.setNextMatch(nextMatch);
                match2.setNextMatch(nextMatch);

                // Mettre à jour les enfants en base
                mRepo.save(match1);
                mRepo.save(match2);

                // Ajouter le parent à la liste du prochain round
                nextRoundMatches.add(nextMatch);
            }
            
            // On passe au round suivant
            currentRoundMatches = nextRoundMatches;
        }
        
        // À la fin, update du statut du tournoi
        tournament.setStatus(Tournament.StatusTournament.EN_COURS);
        tRepo.save(tournament);
    }
    public void enterScore(Long matchId, int scoreA, int scoreB){
        Match match =mRepo.findById(matchId).orElseThrow(()->new RuntimeException("Match introuvable"));
        if(match.getTeamA()==null|| match.getTeamB()==null){
            throw new RuntimeException("Match incomplet:impossible de saisir un score.");
        }
        match.setScoreA(scoreA);
        match.setScoreB(scoreB);
        Team winner;
        if(scoreA>scoreB){
            winner=match.getTeamA();
        }else if(scoreB> scoreA){
            winner=match.getTeamB();
        }else{
            throw new RuntimeException("Match nul interdit! il faut un vainqueur.");
        }
        match.setWinner(winner);
        mRepo.save(match);
        Match nextMatch = match.getNextMatch();
        if(nextMatch!=null){
            if(nextMatch.getTeamA()==null){
                nextMatch.setTeamA(winner);
            }else{
                nextMatch.setTeamB(winner);
            }
            mRepo.save(nextMatch);
        }else{
            Tournament tournament = match.getTournament();
            tournament.setStatus(Tournament.StatusTournament.TERMINE);
            tRepo.save(tournament);
        }
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
}