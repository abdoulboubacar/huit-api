package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.DbArray;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by abdoulbou on 12/12/16.
 */
@Entity
@Table(name = "huit_player_table")
public class Player extends Model {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
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

    @Column
    private Boolean distributor;

    public static Finder<Long, Player> find = new Finder<Long,Player>(Player.class);

    public Player(String name) {
        this.name = name;
        this.valid = true;
        this.winner = false;
        this.distributor = false;
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

    public Boolean getDistributor() {
        return distributor;
    }

    public void setDistributor(Boolean distributor) {
        this.distributor = distributor;
    }
}
