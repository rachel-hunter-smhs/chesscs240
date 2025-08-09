package server.service;

import server.dataaccess.DataAccess;

public class ClearService {
    private final DataAccess dao;
    public ClearService(DataAccess dao) { this.dao = dao; }
    public void clear() { dao.clear(); }
}
