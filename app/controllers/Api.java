package controllers;

import com.google.common.net.MediaType;
import models.Game;
import models.Player;
import play.libs.Json;
import play.mvc.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Api extends Controller {

    private static final Long MAX_GAME = 101L;
    private static final Long BONUS = 10L;
    private static final Long MAX_FIRST_GAME = 50L;

    public Result getGame(String gameName) {
        Game game = Game.findByName(gameName);
        if (game == null) {
            game = new Game(new Date(), gameName);
            game.save();
        }

        return ok(Json.toJson(game)).as(MediaType.JSON_UTF_8.toString());
    }

    public Result addPlayer(String gameName, String name) {
        Game game = Game.findByName(gameName);
        if (game == null) {
            return notFound();
        }
        game.getPlayers().add(new Player(name));
        game.save();

        return ok(Json.toJson(game)).as(MediaType.JSON_UTF_8.toString());
    }

    public Result removePlayer(String gameName, String playerName) {
        Game game = Game.findByName(gameName);
        if (game == null) {
            return notFound();
        }
        game.getPlayers().stream().filter(player -> player.getName().equals(playerName)).forEach(player -> player.delete());
        game.getPlayers().removeIf(player -> player.getName().equals(playerName));
        game.save();

        return ok(Json.toJson(game)).as(MediaType.JSON_UTF_8.toString());
    }

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
                gamePartWinner.getSuperScore().add((long) (game.getPlayers().size()-1));
                gamePartWinner.save();
            });

            List<Player> players = game.getPlayers().stream().sorted((p1, p2) ->
                    (int)(p1.getSuperScore().stream().mapToLong(aLong -> aLong.longValue()).sum()
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
