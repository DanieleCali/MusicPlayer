package elementi;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 22/03/2016.
 */
public class Playlist {
    private String nome;
    private static int nBrani;
    private List<Brano> listaBrani;
    private int codice;

    public Playlist(String nome){
        listaBrani = new ArrayList<>();
        nBrani = 0;
        this.nome = nome;
        this.codice = codice;
    }

    public void aggiungiBrano(Brano brano){
        listaBrani.add(brano);
        nBrani++;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<Brano> getListaBrani() {
        return listaBrani;
    }
}
