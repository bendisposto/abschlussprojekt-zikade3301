package mops.module.database;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import mops.module.services.JsonExclude;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.IndexedEmbedded;

@Entity
@Getter
@Setter
public class Veranstaltung {

    /**
     *  Konstruktor stellt sicher, dass die Sets nicht null sind.
     */
    public Veranstaltung() {
        veranstaltungsformen = new HashSet<>();
        semester = new HashSet<>();
        zusatzfelder = new HashSet<>();
    }

    /**
     *  Konstruktor in dem Felder gesetzt werden, die nicht leer sein dürfen.
     * @param titel Veranstaltungstitel
     * @param leistungspunkte Leistungspunkte für Veranstaltung
     * @param beschreibung Beschreibung der Veranstaltung
     * @param voraussetzungenTeilnahme Teilnahmevoraussetzungen
     */
    public Veranstaltung(
            String titel,
            String leistungspunkte,
            Veranstaltungsbeschreibung beschreibung,
            String voraussetzungenTeilnahme) {
        this.titel = titel;
        this.leistungspunkte = leistungspunkte;
        this.beschreibung = beschreibung;
        this.voraussetzungenTeilnahme = voraussetzungenTeilnahme;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ContainedIn
    @JsonExclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "modul_id")
    private Modul modul;

    @NotBlank(message = "Veranstaltungstitel kann nicht leer sein")
    @Field
    private String titel;

    @NotBlank(message = "Leistungspunkte kann nicht leer sein")
    private String leistungspunkte;

    //Beim Löschen von Veranstaltung werden alle Veranstaltungsformen mitgelöscht
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "veranstaltung",
            orphanRemoval = true)
    private Set<Veranstaltungsform> veranstaltungsformen;

    @Embedded
    @IndexedEmbedded
    @Valid
    private Veranstaltungsbeschreibung beschreibung;

    @NotBlank(message = "Voraussetzungsteilnahme kann nicht leer sein")
    @Field
    @Column(length = 10000)
    private String voraussetzungenTeilnahme;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> semester;

    //Beim Löschen von Veranstaltung werden alle Zusatzfelder mitgelöscht
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "veranstaltung",
            orphanRemoval = true)
    @IndexedEmbedded
    private Set<Zusatzfeld> zusatzfelder;

    public void refreshMapping() {
        this.setZusatzfelder(this.getZusatzfelder());
        this.setVeranstaltungsformen(this.getVeranstaltungsformen());
    }

    /**
     * Überschreibt die Setter & erneuert die Links für die Zusatzfelder.
     *
     * @param zusatzfelder Schon vorhandenes Set von Zusatzfelder
     */
    public void setZusatzfelder(Set<Zusatzfeld> zusatzfelder) {
        if (zusatzfelder == null) {
            return;
        }
        for (Zusatzfeld zusatzfeld : zusatzfelder) {
            zusatzfeld.setVeranstaltung(this);
        }
        this.zusatzfelder = zusatzfelder;
    }

    /**
     * Überschreibt die Setter & erneuert die Links für die Veranstaltungsformen.
     *
     * @param veranstaltungsformen Schon vorhandenes Set von Veranstaltungsformen
     */
    public void setVeranstaltungsformen(Set<Veranstaltungsform> veranstaltungsformen) {
        if (veranstaltungsformen == null) {
            return;
        }
        for (Veranstaltungsform veranstaltungsform : veranstaltungsformen) {
            veranstaltungsform.setVeranstaltung(this);
        }
        this.veranstaltungsformen = veranstaltungsformen;
    }

}
