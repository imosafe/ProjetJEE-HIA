package fr.cytech.pau.hia_jee.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PlanningController {

    @GetMapping("/planning")
    public String showPlanning(
            @RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "year", required = false) Integer year,
            Model model
    ) {
        LocalDate today = LocalDate.now();
        if (month == null) month = today.getMonthValue();
        if (year == null) year = today.getYear();

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        int dayOfWeekValue = firstDayOfMonth.getDayOfWeek().getValue(); 
        LocalDate currentGridDate = firstDayOfMonth.minusDays(dayOfWeekValue - 1);

        List<List<DayCell>> calendarWeeks = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            List<DayCell> week = new ArrayList<>();
            for (int j = 0; j < 7; j++) {
                
                boolean isCurrentMonth = (currentGridDate.getMonthValue() == month);
                
                // Appel de la méthode qui filtre par date
                List<Event> events = getStaticEvents(currentGridDate);

                week.add(new DayCell(
                    currentGridDate.getDayOfMonth(), 
                    isCurrentMonth, 
                    currentGridDate.isEqual(today),
                    events
                ));

                currentGridDate = currentGridDate.plusDays(1);
            }
            calendarWeeks.add(week);
        }

        model.addAttribute("calendarWeeks", calendarWeeks);
        model.addAttribute("currentMonth", yearMonth.getMonth().name());
        model.addAttribute("currentYear", year);
        model.addAttribute("prevMonth", yearMonth.minusMonths(1).getMonthValue());
        model.addAttribute("prevYear", yearMonth.minusMonths(1).getYear());
        model.addAttribute("nextMonth", yearMonth.plusMonths(1).getMonthValue());
        model.addAttribute("nextYear", yearMonth.plusMonths(1).getYear());

        return "planning";
    }

    // --- LOGIQUE MODIFIÉE ICI ---
    private List<Event> getStaticEvents(LocalDate date) {
        List<Event> events = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // RÈGLE MÉTIER : Pas de visibilité au-delà de 3 mois
        // Si la date est avant aujourd'hui OU après (aujourd'hui + 3 mois), on ne renvoie RIEN.
        if (date.isBefore(today) || date.isAfter(today.plusMonths(3))) {
            return events; // Liste vide
        }

        int day = date.getDayOfMonth();
        
        // Si on est dans la période valide, on affiche les faux tournois
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

    public static class DayCell {
        public int day;
        public boolean isCurrentMonth;
        public boolean isToday;
        public List<Event> events;

        public DayCell(int day, boolean isCurrentMonth, boolean isToday, List<Event> events) {
            this.day = day;
            this.isCurrentMonth = isCurrentMonth;
            this.isToday = isToday;
            this.events = events;
        }
    }

    public static class Event {
        public String title;
        public String color;
        public String icon;

        public Event(String title, String color, String icon) {
            this.title = title;
            this.color = color;
            this.icon = icon;
        }
    }
}