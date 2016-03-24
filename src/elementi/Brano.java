package elementi;

/**
 * Created by User on 22/03/2016.
 */
public class Brano {
    private String nome;
    private String percorso;
    private String durata;
    //voto

    public Brano(String nome, String percorso, String durata){
        this.nome = nome;
        this.percorso = percorso;
        this.durata = durata;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPercorso() {
        return percorso;
    }

    public void setPercorso(String percorso) {
        this.percorso = percorso;
    }

    public String getDurata() {
        return durata;
    }

    public void setDurata(String durata) {
        this.durata = durata;
    }
}
