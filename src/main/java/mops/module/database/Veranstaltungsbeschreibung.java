package mops.module.database;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.search.annotations.Field;

@Embeddable
@Data
public class Veranstaltungsbeschreibung {

    /**
     * Konstruktor in dem Felder gesetzt werden, die nicht leer sein dürfen.
     *
     * @param inhalte Inhalte der Veranstaltung
     * @param lernergebnisse Lernergebnisse
     * @param literatur empfohlene Literatur
     * @param verwendbarkeit Verwendbarkeit
     * @param voraussetzungenBestehen Voraussetzungen zum Bestehen
     * @param haeufigkeit Häufigkeit des Angebots
     */
    public Veranstaltungsbeschreibung(
            String inhalte,
            String lernergebnisse,
            String literatur,
            String verwendbarkeit,
            String voraussetzungenBestehen,
            String haeufigkeit) {
        this.inhalte = inhalte;
        this.lernergebnisse = lernergebnisse;
        this.literatur = literatur;
        this.verwendbarkeit = verwendbarkeit;
        this.voraussetzungenBestehen = voraussetzungenBestehen;
        this.haeufigkeit = haeufigkeit;
    }

    public Veranstaltungsbeschreibung(){

    }

    @NotBlank(message = "Veranstaltungsinhalte kann nicht leer sein")
    @Field
    @Column(length = 10000)
    private String inhalte;

    @NotBlank(message = "Lernergebnisse kann nicht leer sein")
    @Field
    @Column(length = 10000)
    private String lernergebnisse;

    @NotBlank(message = "Literatur kann nicht leer sein")
    @Field
    @Column(length = 10000)
    private String literatur;

    @NotBlank(message = "Verwendbarkeit kann nicht leer sein")
    @Field
    @Column(length = 10000)
    private String verwendbarkeit;

    @NotBlank(message = "Voraussetzung zum Bestehen kann nicht leer sein")
    @Field
    @Column(length = 10000)
    private String voraussetzungenBestehen;

    @NotBlank(message = "Häufigkeit kann nicht leer sein")
    @Column(length = 10000)
    private String haeufigkeit;

    @Field
    private String sprache;

}
