/*
 * 
 *  Somado (System Optymalizacji Małych Dostaw)
 *  Optymalizacja dostaw towarów, dane OSM, problem VRP
 * 
 *  Autor: Maciej Kawecki 2016 (praca inż. EE PW)
 * 
 */
package somado;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.sqlite.SQLiteConfig;

/**
 *
 * Ogólny szablon obiektu służącego do komunikacji z bazą danych
 * 
 * @author Maciej Kawecki
 * @version 1.0
 * 
 */
public class Database {
    
    /** Połączenie z bazą danych */
    private Connection connection;
    /** Interfejs do wykonywania zapytań */
    private Statement statement;  
    
    
    public Database() throws SQLException, ClassNotFoundException, NullPointerException, SettingsException {
        
        File f = new File(Settings.getValue("db_name"));
        if(!f.exists() || f.isDirectory()) throw new SettingsException("Brak pliku lokalnej bazy danych "
                + f.getPath());        
        f = new File(Settings.getValue("db_spatial_name"));
        if(!f.exists() || f.isDirectory()) throw new SettingsException("Brak pliku lokalnej bazy danych "
                + "przestrzennych: " + f.getPath());
       
         
        Class.forName("org.sqlite.JDBC");         
        SQLiteConfig config = new SQLiteConfig();
        config.enableLoadExtension(true);
        connection = DriverManager.getConnection("jdbc:sqlite:" 
        		+ Settings.getValue("db_name"), config.toProperties());
       
        statement = connection.createStatement();
        statement.setQueryTimeout(30);
        
        // załadowanie Spatialite i dołączenie b.d. przestrzennych
        statement.execute("SELECT load_extension('mod_spatialite')" );                  
        statement.execute("ATTACH DATABASE '" + Settings.getValue("db_spatial_name") + "' AS spatial;");
        
        
    }
    
    
    
    /**
     * Metoda tworzy prekompilowane zapytanie
     * @param query Tresc zapytania SQL
     * @param genKeys jezeli true, to BD ma zwracac utworzone klucze
     * @return Uchwyt do prekompilowanego zapytania
     * @throws SQLException Blad SQL
     */
    public PreparedStatement prepareQuery(String query, boolean genKeys) throws SQLException {
        
       if (genKeys)
         return connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
       else 
         return connection.prepareStatement(query);
        
    }
       
       
    /**
     * Metoda tworzy prekompilowane zapytanie, nie zwraca kluczy
     * @param query Tresc zapytania SQL
     * @return Uchwyt do prekompilowanego zapytania
     * @throws SQLException Blad SQL
     */    
    public PreparedStatement prepareQuery(String query) throws SQLException {
    
      return prepareQuery(query, false);
      
    }
        
    
    /**
     * Metoda wykonuje zapytanie (SELECT)
     * @param query Zapytanie SQL
     * @return Zestaw wyników
     * @throws SQLException Błąd SQL
     */
    public ResultSet doQuery(String query) throws SQLException {
        
       return statement.executeQuery(query);
   
    }
    
    /**
     * Metoda wykonuje zapytanie (INSERT/UPDATE/DELETE)
     * @param query Zapytanie SQL
     * @throws SQLException Błąd SQL
     */
    public void doUpdate(String query) throws SQLException {
        
       statement.executeUpdate(query);
   
    }    
    
    
    /**
     * Metoda zamyka połączenie z BD
     * @throws SQLException Błąd SQL
     */
    public void close() throws SQLException {
        
      statement.close();  
      connection.close();
   
    }
    
    
    /**
     * Na start transakcji
     * @throws SQLException Błąd SQL
     */
    public void begin() throws SQLException {
    	
      connection.setAutoCommit(false);
    	
    }
    
    
    /**
     * Commit
     * @throws SQLException Błąd SQL
     */    
    public void commit() throws SQLException {
    	
      connection.commit(); 	
      connection.setAutoCommit(true);
    	
    }
    
    
    /**
     * Rollback
     * @throws SQLException Błąd SQL
     */    
    public void rollback() throws SQLException {
    	
      connection.rollback(); 	
      connection.setAutoCommit(true);
    	
    }
    
   
    
}
