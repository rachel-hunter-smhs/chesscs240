package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;

import java.util.List;

public class GameService {
    private final DataAccess dao;
    public GameService(DataAccess d){ dao = d; }

    private String requiredUser(String token) throws DataAccessException {
        if(token==null) throw new DataAccessException("unauthorized");
        AuthData a = dao.getAuth(token);
        if (a==null) throw new DataAccessException("unauthorized");
        return a.username();
    }

    public record CreateRequest(String gameName, String authToken){}
    public record CreateResult(int gameID){}

    public CreateResult create(CreateRequest r) throws DataAccessException{
        requiredUser(r.authToken());
        int id = dao.createGame(r.gameName());
        return new CreateResult(id);
    }

    public record ListRequest(String authToken){}
    public record ListResult(List<GameData> games){}

    public ListResult list(ListRequest r) throws DataAccessException{
        requiredUser(r.authToken());
        return new ListResult(dao.listGames());
    }

    public record JoinRequest(String authToken, String playerColor, int gameID){}

    public void join(JoinRequest r) throws DataAccessException{
        String user = requiredUser(r.authToken());
        if(r.playerColor()==null) throw new DataAccessException("bad request");
        GameData g = dao.getGame(r.gameID());
        if (g == null) throw new DataAccessException("bad request");
        if (r.playerColor().equalsIgnoreCase("WHITE")){
            if (g.whiteUsername()!=null) throw new DataAccessException("already taken");
            dao.saveGame(new GameData(g.gameID(), user, g.blackUsername(), g.gameName(),g.game()));
        } else if (r.playerColor().equalsIgnoreCase("BLACK")) {
            if (g.blackUsername()!= null) throw new DataAccessException("already taken");
            dao.saveGame(new GameData(g.gameID(), g.whiteUsername(), user, g.gameName(), g.game()));

        } else throw new DataAccessException("bad request");
    }











}
