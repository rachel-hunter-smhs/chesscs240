package server.service;
import dataaccess.DataAccess;
import server.dataaccess.DataAccess;

public class ClearService { private final DataAccess dao;
    public ClearService(DataAccess d){dao=d;}public void clear(){dao.clear();}}

