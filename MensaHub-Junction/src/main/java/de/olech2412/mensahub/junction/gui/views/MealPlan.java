package de.olech2412.mensahub.junction.gui.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import de.olech2412.mensahub.junction.gui.components.own.RatingComponent;
import de.olech2412.mensahub.junction.gui.components.own.boxes.InfoBox;
import de.olech2412.mensahub.junction.gui.components.own.boxes.MealBox;
import de.olech2412.mensahub.junction.gui.components.vaadin.datetimepicker.GermanDatePicker;
import de.olech2412.mensahub.junction.gui.components.vaadin.notifications.NotificationFactory;
import de.olech2412.mensahub.junction.gui.components.vaadin.notifications.types.InfoNotification;
import de.olech2412.mensahub.junction.gui.components.vaadin.notifications.types.InfoWithAnchorNotification;
import de.olech2412.mensahub.junction.gui.components.vaadin.notifications.types.NotificationType;
import de.olech2412.mensahub.junction.jpa.repository.RatingRepository;
import de.olech2412.mensahub.junction.jpa.repository.UsersRepository;
import de.olech2412.mensahub.junction.jpa.services.MailUserService;
import de.olech2412.mensahub.junction.jpa.services.RatingService;
import de.olech2412.mensahub.junction.jpa.services.meals.MealsService;
import de.olech2412.mensahub.junction.jpa.services.mensen.MensaService;
import de.olech2412.mensahub.models.Meal;
import de.olech2412.mensahub.models.Mensa;
import de.olech2412.mensahub.models.Rating;
import de.olech2412.mensahub.models.authentification.MailUser;
import de.olech2412.mensahub.models.result.Result;
import de.olech2412.mensahub.models.result.errors.jpa.JPAError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Route("mealPlan")
@PageTitle("Speiseplan")
@AnonymousAllowed
@Slf4j
public class MealPlan extends VerticalLayout implements BeforeEnterObserver {

    private final MealsService mealsService;

    private final MensaService mensaService;

    private final ComboBox<Mensa> mensaComboBox = new ComboBox<>();

    private final GermanDatePicker datePicker = new GermanDatePicker();

    private List<Meal> meals;

    HorizontalLayout row = new HorizontalLayout();

    @Autowired
    RatingRepository ratingRepository;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private MailUserService mailUserService;

    private MailUser mailUser;
    @Autowired
    private RatingService ratingService;

    public MealPlan(MealsService mealsService, MensaService mensaService) {
        this.mealsService = mealsService;
        this.mensaService = mensaService;

        datePicker.setValue(LocalDate.now());

        HorizontalLayout pageSelHeader = new HorizontalLayout();
        mensaComboBox.setItems(mensaService.getAllMensas());
        mensaComboBox.setItemLabelGenerator(Mensa::getName);

        VerticalLayout headerComboboxLayout = new VerticalLayout();
        headerComboboxLayout.setWidth(100f, Unit.PERCENTAGE);
        headerComboboxLayout.setAlignItems(Alignment.CENTER);
        headerComboboxLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        headerComboboxLayout.add(mensaComboBox, datePicker);

        //adjust the width of the combobox and datePicker
        mensaComboBox.setWidth(60f, Unit.PERCENTAGE);
        mensaComboBox.setMinWidth(320f, Unit.PIXELS);
        datePicker.setWidth(40f, Unit.PERCENTAGE);
        datePicker.setMinWidth(320f, Unit.PIXELS);

        VerticalLayout headerContent = new VerticalLayout();
        headerContent.setWidth(100f, Unit.PERCENTAGE);
        headerContent.setAlignItems(Alignment.CENTER);
        headerContent.setJustifyContentMode(JustifyContentMode.CENTER);
        headerContent.getStyle().set("text-align", "center");
        headerContent.add(new H2("Wähle deine Mensa aus, sowie das gewünschte Datum"));
        headerContent.add(headerComboboxLayout);

        pageSelHeader.add(headerContent);


        row.setJustifyContentMode(JustifyContentMode.CENTER);

        mensaComboBox.addValueChangeListener(changeEvent -> {
            if (changeEvent.getValue() == null) {
                return;
            }
            row.removeAll();
            meals = mealsService.findAllMealsByServingDateAndMensa(datePicker.getValue(), changeEvent.getValue());

            row.addClassName("meal-content");
            row.add(new InfoBox(changeEvent.getValue().getName(),
                    changeEvent.getValue().getApiUrl().replace("&date=$date", "")));
            row.addClassName("meal-row");
            row.setWidthFull();
            row.getStyle().set("flex-wrap", "wrap");

            updateRows(meals);

            if(mailUser == null){
                UI.getCurrent().getPage().getHistory().replaceState(null, String.format("/mealPlan?mensa=%s" +
                        String.format("&date=%s", datePicker.getValue()), changeEvent.getValue().getId()));
            } else {
                UI.getCurrent().getPage().getHistory().replaceState(null, String.format("/mealPlan?date=%s" +
                        String.format("&mensa=%s", mensaComboBox.getValue().getId()) + String.format("&userCode=%s",
                        mailUser.getDeactivationCode().getCode()), datePicker.getValue()));
            }
            add(row);
        });

        datePicker.addValueChangeListener(changeEvent -> {
            if (changeEvent.getValue() == null || mensaComboBox.isEmpty()) {
                return;
            }

            row.removeAll();
            meals = mealsService.findAllMealsByServingDateAndMensa(changeEvent.getValue(), mensaComboBox.getValue());

            row.addClassName("meal-content");
            row.add(new InfoBox(mensaComboBox.getValue().getName(),
                    mensaComboBox.getValue().getApiUrl().replace("$date", changeEvent.getValue().toString())));
            row.addClassName("meal-row");
            row.setWidthFull();
            row.getStyle().set("flex-wrap", "wrap");

            updateRows(meals);

            if(mailUser == null) {
                UI.getCurrent().getPage().getHistory().replaceState(null, String.format("/mealPlan?date=%s" +
                        String.format("&mensa=%s", mensaComboBox.getValue().getId()), changeEvent.getValue()));
            } else {
                UI.getCurrent().getPage().getHistory().replaceState(null, String.format("/mealPlan?date=%s" +
                        String.format("&mensa=%s", mensaComboBox.getValue().getId()) + String.format("&userCode=%s",
                        mailUser.getDeactivationCode().getCode()), changeEvent.getValue()));
            }
            add(row);
        });

        add(pageSelHeader);

        // adjust the layout to center all elements in it
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }

    private void updateRows(List<Meal> meals) {
        for (Meal meal : meals) {
            MealBox mealBox = new MealBox(meal.getName(), meal.getDescription(), meal.getPrice(), meal.getAllergens(), meal.getCategory());
            if(!isUserIdentified()){
                mealBox.getRatingComponent().setEnabled(false);
                mealBox.getRatingButton().setEnabled(false);
            } else {
                Result<List<Rating> , JPAError> ratings = ratingService.findAllByMailUserAndMealName(mailUser, meal.getName());

                if (ratings.isSuccess()){
                    List<Rating> ratingList = ratings.getData();
                    for (Rating rating : ratingList) {
                        if (rating.getMealName().equals(meal.getName()) && rating.getMeal().getServingDate().equals(meal.getServingDate())
                        && rating.getMeal().getMensa().getId().equals(meal.getMensa().getId())) {
                            mealBox.getRatingButton().setEnabled(false);
                            mealBox.getRatingComponent().setEnabled(false);
                            mealBox.getRatingComponent().setRating(rating.getRating());
                        }
                    }
                } else {
                    NotificationFactory.create(NotificationType.ERROR, "Wir haben Schwierigkeiten, deine Ratings " +
                            "abzurufen. Die Funktion ist daher deaktiviert.").open();
                }

                mealBox.getRatingButton().addClickListener(buttonClickEvent -> {
                    if (mealBox.getRatingComponent().getRating() != 0){
                        Rating rating = new Rating();
                        rating.setMeal(meal);
                        rating.setRating(mealBox.getRatingComponent().getRating());
                        rating.setMealName(meal.getName());
                        rating.setMailUser(mailUser);
                        ratingRepository.save(rating);
                        mealBox.getRatingButton().setEnabled(false);
                        mealBox.getRatingComponent().setEnabled(false);
                    }
                });
            }
            row.add(mealBox);
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        Map<String, List<String>> params = beforeEnterEvent.getLocation().getQueryParameters().getParameters();

        String mensaParam;
        String date = "";
        String userCode;

        if(params.containsKey("userCode")){
            userCode = params.get("userCode").get(0);

            Result<MailUser, JPAError> mailUserJPAErrorResult = mailUserService.findMailUserByDeactivationCode(userCode);

            if(mailUserJPAErrorResult.isSuccess()){
                mailUser = mailUserJPAErrorResult.getData();
                log.info("Mail User erfolgreich identifiziert");
            } else {
                NotificationFactory.create(NotificationType.ERROR, "Ungültige Nutzerkennung. Erweiterte Funktionen stehen nicht zur Verfügung");
            }
        } else {
            UI.getCurrent().getPage().executeJs("return !!window.localStorage.getItem('infoNotificationShown');")
                    .then(jsonValue -> {
                        boolean isShown = jsonValue.asBoolean();
                        if (!isShown) {
                            InfoWithAnchorNotification infoNotification = new InfoWithAnchorNotification(
                                    "Mit einem MensaHub-Account kannst das Essen zusätzlich bewerten und dir Empfehlungen berechnen lassen -> ",
                                    "Zur Anmeldung", NewsletterView.class);

                            infoNotification.getCloseButton().addClickListener(event -> {
                                infoNotification.close();
                                // Speichern, dass die Nachricht angezeigt wurde
                                UI.getCurrent().getPage().executeJs("window.localStorage.setItem('infoNotificationShown', 'true');");
                            });
                            infoNotification.addThemeVariants(NotificationVariant.LUMO_WARNING);

                            infoNotification.open();
                        }
                    });
        }

        if (params.containsKey("mensa")) {
            mensaParam = params.get("mensa").get(0);
        } else {
            return;
        }

        if (params.containsKey("date")) {
            date = params.get("date").get(0);
        }

        Optional<Mensa> mensa;
        // check if mensaParam is an integer
        try {
            mensa = Optional.of(mensaService.mensaById(Long.parseLong(mensaParam)));
        } catch (NumberFormatException numberFormatException) {
            mensa = mensaService.findAll().stream().filter(mensa1 -> mensa1.getName().equals(mensaParam)).findAny();
        }

        Optional<Mensa> finalMensa = mensa;
        mensa.ifPresent(value -> mensaComboBox.setValue(finalMensa.get()));

        if (!date.isEmpty()) {
            if (date.equals("today")) {
                datePicker.setValue(LocalDate.now());
            } else {
                datePicker.setValue(LocalDate.parse(date));
            }
        }
    }

    private boolean isUserIdentified(){
        return mailUser != null;
    }
}