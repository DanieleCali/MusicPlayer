package elementi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 22/03/2016.
 */
public class Playlist implements Serializable{
    private String nome;
    private List<Brano> listaBrani;

    public Playlist(String nome, List<Brano> listaBrani){
        if (listaBrani == null)  this.listaBrani = new ArrayList<>();
        else this.listaBrani = listaBrani;
        this.nome = nome;
    }

    public void aggiungiBrano(Brano brano){
        listaBrani.add(brano);
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
