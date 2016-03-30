package elementi;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by User on 23/03/2016.
 */
public class TableRow {
    private final SimpleStringProperty titolo;
    private final SimpleStringProperty durata;
    private final SimpleStringProperty percorso;
    private final SimpleIntegerProperty codice;

    public TableRow(int codice, String titolo, String durata, String percorso){
        this.codice = new SimpleIntegerProperty(codice);
        this.titolo = new SimpleStringProperty(titolo);
        this.durata = new SimpleStringProperty(durata);
        this.percorso = new SimpleStringProperty(percorso);
    }

    public String getTitolo() {
        return titolo.get();
    }

    public SimpleStringProperty titoloProperty() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo.set(titolo);
    }

    public String getDurata() {
        return durata.get();
    }

    public SimpleStringProperty durataProperty() {
        return durata;
    }

    public void setDurata(String durata) {
        this.durata.set(durata);
    }

    public String getPercorso() {
        return percorso.get();
    }

    public SimpleStringProperty percorsoProperty() {
        return percorso;
    }

    public void setPercorso(String percorso) {
        this.percorso.set(percorso);
    }

    public int getCodice() {
        return codice.get();
    }

    public SimpleIntegerProperty codiceProperty() {
        return codice;
    }

    public void setCodice(int codice) {
        this.codice.set(codice);
    }
}
