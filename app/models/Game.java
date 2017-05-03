package models;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by abdoulbou on 12/12/16.
 */
@Entity
@Table(name = "huit_game_table")
public class Game extends Model {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    public Long id;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Player> players;

    private String name;

    private Date date;

    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private User owner;

    public static Finder<Long, Game> find = new Finder<Long,Game>(Game.class);

    public Game(String name) {
        this.date = new Date();
        this.name = name;
        this.players = new ArrayList<Player>();
    }

    public static Game findByName(String name) {
        ExpressionList<Game> res = find.where().eq("name", name);
        return res.findUnique();
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
