package de.olech2412.mensahub.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * This class represents the mensa it is the super class for all mensas.
 */
@Getter
@Setter
@Entity
@Table(name = "mensas")
public class Mensa {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @EqualsAndHashCode.Exclude
    @Column(name = "id", nullable = false)
    private Long id; // this is the primary key

    private String name; // this is the name of the mensa

    private String apiUrl; // this is the url to the api of the mensa

    @OneToMany(mappedBy = "mensas_id")
    @JsonIgnore
    @Transient
    private Set<Meal> mealSet;

    /**
     * This is the default constructor.
     */
    public Mensa() {
    }

    /**
     * This is the constructor with all parameters.
     * @param name the name of the mensa
     * @param apiUrl the url to the api of the mensa
     */
    public Mensa(Long id, String name, String apiUrl) {
        this.id = id;
        this.name = name;
        this.apiUrl = apiUrl;
    }
}