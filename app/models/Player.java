package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.DbArray;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by abdoulbou on 12/12/16.
 */
@Entity
public class Player extends Model {

    @Id
    public Long id;

    @DbArray
    private List<Long> score;

    @DbArray
    private List<Long> superScore;

    @Column
    private String name;

    @Column
    private Boolean valid;

    @Column
    private Boolean winner;

    public static Finder<Long, Player> find = new Finder<Long,Player>(Player.class);

    public Player(String name) {
        this.name = name;
        this.valid = true;
        this.winner = false;
        setSuperScore(new ArrayList<Long>());
        setScore(new ArrayList<Long>());
    }

    public Long getTotal() {
        return this.getScore().stream().mapToLong(aLong -> aLong.longValue()).sum();
    }

    public List<Long> getScore() {
        return score;
    }

    public void setScore(List<Long> scrore) {
        this.score = scrore;
    }

    public List<Long> getSuperScore() {
        return superScore;
    }

    public void setSuperScore(List<Long> superScore) {
        this.superScore = superScore;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public Boolean isWinner() {
        return winner;
    }

    public void setWinner(Boolean winner) {
        this.winner = winner;
    }
}
