package de.olech2412.mensahub.datadispatcher.jpa.repository.Leipzig.meals;

import de.olech2412.mensahub.models.Leipzig.meals.Meals_Menseria_am_Botanischen_Garten;
import org.springframework.data.repository.ListCrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface Meals_Menseria_am_Botanischen_GartenRepository extends ListCrudRepository<Meals_Menseria_am_Botanischen_Garten, Long> {

    List<Meals_Menseria_am_Botanischen_Garten> findAllMealsByServingDateGreaterThanEqual(LocalDate servingDate);

    List<Meals_Menseria_am_Botanischen_Garten> findAllMealsByServingDate(LocalDate servingDate);

    void deleteAllByServingDate(LocalDate servingDate);

    List<Meals_Menseria_am_Botanischen_Garten> findMeals_Menseria_am_Botanischen_GartenByNameAndServingDateBeforeOrderByServingDateDesc(String name, LocalDate servingDate);

}