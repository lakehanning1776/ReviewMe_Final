package com.example.demo.service;

import com.example.demo.model.Game;
import java.util.List;

public interface GameService {

    List<Game> getAllGames();

    Game getGameById(Long id);

    Game saveGame(Game game);

    void deleteGame(Long id);
}