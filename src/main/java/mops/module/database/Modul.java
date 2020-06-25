package mops.module.database;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import mops.module.searchconfig.IndexWhenVisibleInterceptor;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Getter
@Setter
@Indexed(interceptor = IndexWhenVisibleInterceptor.class)
public class Modul {

    public Modul(){

    }

    /**
     * Konstruktor in dem Felder gesetzt werden, die nicht leer sein dürfen.
     *
     * @param titelDeutsch deutscher Modultitel
     * @param titelEnglisch englischer Modultitel
     * @param modulbeauftragte die Modulbeauftragte
     * @param gesamtLeistungspunkte Leistungspunkte für dieses Modul
     * @param studiengang Studiengang, dem Modul zugeordnet wird
     * @param modulkategorie Modulkategorie
     */
    public Modul(String titelDeutsch,
                 String titelEnglisch,
                 String modulbeauftragte,
                 String gesamtLeistungspunkte,
                 String studiengang,
                 Modulkategorie modulkategorie) {
        this.titelDeutsch = titelDeutsch;
        this.titelEnglisch = titelEnglisch;
        this.modulbeauftragte = modulbeauftragte;
        this.gesamtLeistungspunkte = gesamtLeistungspunkte;
        this.studiengang = studiengang;
        this.modulkategorie = modulkategorie;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Modultitel kann nicht leer sein")
    @Field
    private String titelDeutsch;

    @NotBlank(message = "Englischer Modultitel kann nicht leer sein")
    @Field
    private String titelEnglisch;

    //Beim Löschen von Modul werden alle Veranstaltungen mitgelöscht, daher ist CascadeType.ALL
    //und FetchType.EAGER gewünscht
    @IndexedEmbedded
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "modul")
    private Set<Veranstaltung> veranstaltungen;

    @NotBlank(message = "Modulbeauftragter kann nicht leer sein")
    @Field
    private String modulbeauftragte;

    @NotBlank(message = "Leistungspunkte kann nicht leer sein")
    private String gesamtLeistungspunkte;

    @NotBlank(message = "Studiengang kann nicht leer sein")
    @Field
    private String studiengang;

    @NotNull(message = "Geben Sie die Modulkategorie an!")
    private Modulkategorie modulkategorie;

    private Boolean sichtbar;

    @DateTimeFormat(pattern = "dd.MM.yyyy, HH:mm:ss")
    private LocalDateTime datumErstellung;

    @DateTimeFormat(pattern = "dd.MM.yyyy, HH:mm:ss")
    private LocalDateTime datumAenderung;

    /**
     * Ruft setVeranstaltungen & setZusatzfelder auf, um das Mapping zu erneuern.
     */
    public void refreshMapping() {
        this.setVeranstaltungen(this.getVeranstaltungen());
        for (Veranstaltung v : veranstaltungen) {
            v.refreshMapping();
        }
    }

    /**
     * Fügt eine Veranstaltung zum Modul hinzu.
     *
     * @param veranstaltung Neue Veranstaltung
     */
    public void addVeranstaltung(Veranstaltung veranstaltung) {
        if (veranstaltungen == null) {
            veranstaltungen = new HashSet<>();
        }
        veranstaltungen.add(veranstaltung);
        veranstaltung.setModul(this);
    }

    /**
     * Überschreibt die Setter & erneuert die Links für die Veranstaltungen.
     *
     * @param veranstaltungen Schon vorhandenes von Veranstaltungen
     */
    public void setVeranstaltungen(Set<Veranstaltung> veranstaltungen) {
        if (veranstaltungen == null) {
            return;
        }
        for (Veranstaltung veranstaltung : veranstaltungen) {
            veranstaltung.setModul(this);
        }
        this.veranstaltungen = veranstaltungen;
    }

}
