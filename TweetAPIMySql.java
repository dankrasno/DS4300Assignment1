import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.time.Duration;
import java.time.Instant;

/**
 * A class implementing TweetAPI for MySQL databases
 */
public class TweetAPIMySql implements TweetAPI {
    private Connection con = null;

    /**
     * Constructor for API object
     * @param url the URL the database is located
     * @param user the username being used to connect to the database
     * @param password the password being used to connect to the database
     */
    TweetAPIMySql(String url, String user, String password) throws IOException, SQLException {
        this.con = this.getConnection(url, user, password);
        this.importFollowers();
    }

    /**
     * Used to create a connection with the MySQL database
     * @param url the location of the database
     * @param user the username for the database
     * @param password the password for the database
     * @return a connection object to the database
     */
    private Connection getConnection(String url, String user, String password) {
        if (con == null) {
            try {
                con = DriverManager.getConnection(url, user, password);
                return con;
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }
        return con;
    }

    @Override
    public void postTweet(Tweet t) {

        String sql = "INSERT into Tweets (user_id, tweets_ts, tweets_text) " +
                "VALUES (" + t.getUserID() + ", \"" + Timestamp.valueOf(t.getDate()) + "\", \"" + t.getContent() + "\");";

        System.out.println(sql);
        try {
            Statement s = con.createStatement();
            s.execute(sql);
            con.commit();
            s.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getTimeline(int userID) {

        String sql = "select t.* FROM Tweets t join follows f on t.user_id = f.follows_id " +
                "where f.follower_id = " + userID + " order by t.datetime desc limit 20";

        try {
            // get connection and initialize statement
            Statement s = this.con.createStatement();
            ResultSet rs = s.executeQuery(sql);
            while (rs.next()) {
                rs.getString("tweets_text");
            }
            rs.close();
            s.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

    }

    private void importFollowers() throws IOException, SQLException {
        BufferedReader csvReader = new BufferedReader(new FileReader("new.csv"));
        String row;

        // wipe the old followers
        Statement ss = this.con.createStatement();
        ss.execute("TRUNCATE TABLE tweets");
        ss.execute("TRUNCATE TABLE followers");
        ss.close();

        while ((row = csvReader.readLine()) != null) {
            String[] data = row.split(",");
            String sql = "insert into followers (user_id, follows_id) VALUES ("
                    + Integer.parseInt(data[0])+ ", " + Integer.parseInt(data[1]) + ");";
            Statement s = this.con.createStatement();
            s.execute(sql);
            s.close();
        }
        System.out.println("Followers import successful");
    }

}