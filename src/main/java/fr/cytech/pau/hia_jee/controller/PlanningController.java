package fr.cytech.pau.hia_jee.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

//Contrôleur gérant l'affichage de la page "Planning".
 
@Controller
public class PlanningController {

    // Point d'entrée pour afficher le calendrier.
     
    @GetMapping("/planning")
    public String showPlanning(
            @RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "year", required = false) Integer year,
            Model model
    ) {
        LocalDate today = LocalDate.now();

        // Si aucun mois/année n'est fourni, on prend la date d'aujourd'hui
        if (month == null) month = today.getMonthValue();
        if (year == null) year = today.getYear();

        // Création de l'objet YearMonth pour manipuler le mois demandé
        YearMonth yearMonth = YearMonth.of(year, month);
        
        // On récupère le 1er jour du mois
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        
        // Calcul du décalage pour commencer la grille le Lundi.
        // getValue() renvoie 1 pour Lundi, 7 pour Dimanche.
        // On recule d'autant de jours pour trouver le début de la première semaine visible.
        int dayOfWeekValue = firstDayOfMonth.getDayOfWeek().getValue(); 
        LocalDate currentGridDate = firstDayOfMonth.minusDays(dayOfWeekValue - 1);

        // Liste contenant les 6 semaines du calendrier
        List<List<DayCell>> calendarWeeks = new ArrayList<>();

        // On génère 6 semaines pour couvrir tous les cas de débordement de mois
        for (int i = 0; i < 6; i++) {
            List<DayCell> week = new ArrayList<>();
            // Boucle sur les 7 jours de la semaine
            for (int j = 0; j < 7; j++) {
                
                // Vérifie si le jour traité appartient bien au mois demandé (pour le style CSS grisé par ex.)
                boolean isCurrentMonth = (currentGridDate.getMonthValue() == month);
                
                // Récupération des événements pour ce jour spécifique
                // (C'est ici que la règle des 3 mois s'applique)
                List<Event> events = getStaticEvents(currentGridDate);

                // Création de la cellule (DTO) pour la vue
                week.add(new DayCell(
                    currentGridDate.getDayOfMonth(), 
                    isCurrentMonth, 
                    currentGridDate.isEqual(today), // Marqueur pour surligner "aujourd'hui"
                    events
                ));

                // Passage au jour suivant
                currentGridDate = currentGridDate.plusDays(1);
            }
            calendarWeeks.add(week);
        }

        // Ajout des données au modèle pour l'affichage HTML
        model.addAttribute("calendarWeeks", calendarWeeks);
        model.addAttribute("currentMonth", yearMonth.getMonth().name()); // Nom du mois (ex: JANUARY)
        model.addAttribute("currentYear", year);
        
        // Calculs pour les boutons "Mois précédent" et "Mois suivant"
        model.addAttribute("prevMonth", yearMonth.minusMonths(1).getMonthValue());
        model.addAttribute("prevYear", yearMonth.minusMonths(1).getYear());
        model.addAttribute("nextMonth", yearMonth.plusMonths(1).getMonthValue());
        model.addAttribute("nextYear", yearMonth.plusMonths(1).getYear());

        return "planning";
    }

    //Génère une liste d'événements statiques (hardcodés) pour une date donnée.
    
    private List<Event> getStaticEvents(LocalDate date) {
        List<Event> events = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // --- RÈGLE MÉTIER ---
        // On ne montre les tournois que si la date est comprise entre :
        // [Aujourd'hui] et [Aujourd'hui + 3 mois].
        // Si la date est passée OU si elle est trop loin dans le futur, on retourne vide.
        if (date.isBefore(today) || date.isAfter(today.plusMonths(3))) {
            return events; 
        }

        int day = date.getDayOfMonth();
        
        // Si on est dans la période valide, on ajoute des événements fictifs selon le numéro du jour
        if (day == 5) {
            events.add(new Event("LoL Worlds", "danger", "bi-trophy-fill"));
        }
        if (day == 12) {
            events.add(new Event("Valorant Cup", "info", "bi-mouse"));
        }
        if (day == 19) {
            events.add(new Event("CS:GO Major", "warning", "bi-crosshair"));
        }
        if (day == 25) {
            events.add(new Event("Rocket League", "primary", "bi-car-front-fill"));
        }

        return events;
    }

    // Classe interne  représentant une case du calendrier (un jour).

    public static class DayCell {
        public int day;                 // Numéro du jour (1-31)
        public boolean isCurrentMonth;  // Est-ce que ce jour fait partie du mois affiché ?
        public boolean isToday;         // Est-ce aujourd'hui ?
        public List<Event> events;      // Liste des événements du jour

        public DayCell(int day, boolean isCurrentMonth, boolean isToday, List<Event> events) {
            this.day = day;
            this.isCurrentMonth = isCurrentMonth;
            this.isToday = isToday;
            this.events = events;
        }
    }

    /**
     * Classe interne représentant un événement sportif.
     */
    public static class Event {
        public String title;  // Nom de l'événement
        public String color;  // Classe couleur Bootstrap (ex: danger, info, primary)
        public String icon;   // Classe icône Bootstrap Icons (ex: bi-trophy-fill)

        public Event(String title, String color, String icon) {
            this.title = title;
            this.color = color;
            this.icon = icon;
        }
    }
}