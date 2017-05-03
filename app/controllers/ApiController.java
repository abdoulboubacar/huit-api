package controllers;

import com.google.common.net.MediaType;
import form.GameForm;
import form.LoginForm;
import form.PlayerForm;
import models.Game;
import models.Player;
import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.*;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ApiController extends Controller {

    private static final Long MAX_GAME = 101L;
    private static final Long BONUS = 10L;
    private static final Long MAX_FIRST_GAME = 50L;

    @Inject
    private FormFactory formFactory;

    public Result getGame(Long id) {
        return ok(Json.toJson(Game.find.byId(id))).as(MediaType.JSON_UTF_8.toString());
    }

    public Result getAllGames() {
        return ok(Json.toJson(Game.find.all())).as(MediaType.JSON_UTF_8.toString());
    }

    @Security.Authenticated(Secured.class)
    public Result createGame() {
        Form<GameForm> gameForm = formFactory.form(GameForm.class).bindFromRequest();
        if (gameForm.hasErrors()) {
            return badRequest(gameForm.errorsAsJson());
        }

        Game game = new Game(gameForm.get().getName());
        game.setOwner(SecurityController.getUser());
        game.save();

        return created(Json.toJson(game)).as(MediaType.JSON_UTF_8.toString());
    }

    @Security.Authenticated(Secured.class)
    public Result addPlayer(Long gameId) {
        Game game = Game.find.byId(gameId);
        if (game == null) {
            return notFound();
        }
        if (!game.getOwner().equals(SecurityController.getUser())) {
            return unauthorized();
        }
        Form<PlayerForm> playerForm = formFactory.form(PlayerForm.class).bindFromRequest();
        Player player = new Player(playerForm.get().getName());
        game.getPlayers().add(player);
        game.save();

        return ok(Json.toJson(game)).as(MediaType.JSON_UTF_8.toString());
    }

    @Security.Authenticated(Secured.class)
    public Result removePlayer(Long gameId, Long playerId) {
        Game game = Game.find.byId(gameId);
        if (game == null) {
            return notFound();
        }
        if (!game.getOwner().equals(SecurityController.getUser())) {
            return unauthorized();
        }
        game.getPlayers().stream().filter(player -> player.id.equals(playerId)).forEach(player -> player.delete()); // remove db
        game.getPlayers().removeIf(player -> player.id.equals(playerId)); // remove into collection
        game.save();

        return ok(Json.toJson(game)).as(MediaType.JSON_UTF_8.toString());
    }

    @Security.Authenticated(Secured.class)
    public Result removeAllPlayers(Long gameId) {
        Game game = Game.find.byId(gameId);
        if (game == null) {
            return notFound();
        }
        if (!game.getOwner().equals(SecurityController.getUser())) {
            return unauthorized();
        }
        game.getPlayers().stream().forEach(player -> player.delete());
        game.getPlayers().clear();
        game.save();

        return ok(Json.toJson(game)).as(MediaType.JSON_UTF_8.toString());
    }

    @Security.Authenticated(Secured.class)
    public Result updatePlayerScore(String gameName, String playerName, Long score) {
        Game game = Game.findByName(gameName);
        if (game == null) {
            return notFound();
        }
        Player player = game.getPlayers().stream().filter(p -> p.getName().equals(playerName)).findFirst().get();
        int nbGame = player.getScore().size();
        if (nbGame >= 2
                && player.getScore().get(nbGame - 1) == 0
                && player.getScore().get(nbGame - 2) == 0
                && score == 0) {
            //on offre le bonus de points au joueur qui gagne trois fois d'affilée
            score = score - BONUS;
        }

        if (player.getTotal() + score == MAX_GAME) {
            //on offre le bonus de points au joueur qui comptabilise exactement $MAX_GAME=101 par défaut
            score = score - BONUS;
        }

        if (player.getTotal() + score > MAX_GAME) {
            //le joueur dont les points depassent $MAX_GAME est éliminé
            player.setValid(false);
        }

        if (player.getScore().isEmpty() && score >= MAX_FIRST_GAME) {
            //le joueur dont les points au premier jeu dépassent $MAX_FIRST_GAME est éliminé
            player.setValid(false);
        }

        player.getScore().add(score);
        player.save();
        game.save();
        tryClosingGame(game);

        return ok(Json.toJson(game)).as(MediaType.JSON_UTF_8.toString());
    }

    @Security.Authenticated(Secured.class)
    public Result deleteLastLine(String gameName) {
        Game game = Game.findByName(gameName);
        Integer max = game.getPlayers().stream().max((player, t1) -> player.getScore().size() - t1.getScore().size()).get().getScore().size();
        if (max == 0) {
            return notFound();
        }
        game.getPlayers().stream().filter(player -> player.getScore().size() == max).forEach(player -> {
            player.getScore().remove(max - 1);
            if (player.getTotal() < MAX_GAME) {
                player.setValid(true); //on remet le joueur dans le jeu
            }
            player.save();
        });

        game.save();

        return ok(Json.toJson(game)).as(MediaType.JSON_UTF_8.toString());
    }

    @Security.Authenticated(Secured.class)
    public Result updatePlayerSuperScore(String gameName, String playerName, Long superscore) {
        Game game = Game.findByName(gameName);
        if (game == null) {
            return notFound();
        }
        Player player = game.getPlayers().stream().filter(p -> p.getName().equals(playerName)).findFirst().get();
        player.getSuperScore().add(superscore);
        player.save();
        game.save();

        return ok(Json.toJson(game)).as(MediaType.JSON_UTF_8.toString());
    }

    @Security.Authenticated(Secured.class)
    public Result playAgain(String gameName) {
        Game game = Game.findByName(gameName);
        if (game == null) {
            return notFound();
        }
        game.getPlayers().stream().forEach(player -> {
            player.getScore().clear();
            player.setValid(true);
            player.save();
        });
        game.save();
        return ok(Json.toJson(game)).as(MediaType.JSON_UTF_8.toString());
    }

    private void tryClosingGame(Game game) {
        if (game.getPlayers().stream().filter(player -> player.isValid()).count() == 1) {
            List<Player> notValidPlayers = game.getPlayers().stream().filter(streamPlayer -> !streamPlayer.isValid()).sorted((p1, p2) -> {
                if (p2.getScore().size() == p1.getScore().size()) {
                    return (int) (p2.getTotal() - p1.getTotal());
                } else {
                    return p1.getScore().size() - p2.getScore().size();
                }
            }).collect(Collectors.toList());

            notValidPlayers.stream().forEach(player -> {
                player.getSuperScore().add((long) notValidPlayers.indexOf(player));
                player.save();
            });

            game.getPlayers().stream().filter(player -> player.isValid()).forEach(gamePartWinner -> {
                gamePartWinner.setValid(false);
                gamePartWinner.getSuperScore().add((long) (game.getPlayers().size() - 1));
                gamePartWinner.save();
            });

            List<Player> players = game.getPlayers().stream().sorted((p1, p2) ->
                    (int) (p1.getSuperScore().stream().mapToLong(aLong -> aLong.longValue()).sum()
                            - p2.getSuperScore().stream().mapToLong(aLong -> aLong.longValue()).sum())).collect(Collectors.toList());

            players.stream().peek(winner -> {
                winner.setWinner(true);
                winner.save();
            });

            players.stream().forEach(player -> {
                player.setWinner(false);
                player.save();
            });

            game.save();
        }
    }

}
