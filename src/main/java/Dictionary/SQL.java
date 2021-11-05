package Dictionary;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.io.IOException;

public class SQL {

    /**
     * url: dataname
     */
    public String url = "jdbc:mysql://localhost:3306/data";
    public String username = "root";
    public String password = "haiduy39";
    public String table = "tbl_edict";
    public Connection connection;
    public static List<String> wordList = new ArrayList<>();
    // tạo 1 array mới để thêm cả từ và nghĩa
    public static List<Word> list= new ArrayList<>();
    public int num;


    public void connect() {
        try {
            connection = DriverManager.getConnection(url, username, password);
            num = getNumber();
            System.out.println("Connected to database");
        } catch (Exception e) {
            System.out.println("Error");
            e.printStackTrace();
        }
    }

    public int getNumber() {
        int n = 0;
        ResultSet rs = null;
        String sqlCommand = "SELECT * FROM " + table + " ORDER BY idx DESC LIMIT 1";
        Statement st;
        try {
            st = connection.createStatement();
            rs = st.executeQuery(sqlCommand);
            rs.next();
            n = rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Select error");
            e.printStackTrace();
        }
        return n;
    }

//    public String showData(ResultSet rs) throws SQLException {
//        String tmp = "";
//        while (rs.next()) {
//            tmp += rs.getString(3);
////            wordList.add(rs.getString(3));
////            System.out.printf("%-10s %-20s %-20s \n", rs.getString(1), rs.getString(2), rs.getString(3));
//        }
//        return tmp;
//    }

    /**
     * trả về tất cả các từ.
     */
    public ResultSet getAllWord() {
        ResultSet rs = null;
        String sqlCommand = "select * from " + table;
        Statement st;
        try {
            st = connection.createStatement();
            rs = st.executeQuery(sqlCommand);
        } catch (SQLException e) {
            System.out.println("Select error");
            e.printStackTrace();
        }
        return rs;
    }

    //thêm từ vào list và wordList
    public void wordList() throws SQLException {
        connect();
        ResultSet rs = getAllWord();
        while (rs.next()) {
            wordList.add(rs.getString(2));
            Word word = new Word();
            word.word_target = rs.getString(2);
            word.word_explain = rs.getString(3);
            list.add(word);
        }
    }

    /**
     * Trả về từ cần tìm.
     */
    public ResultSet getWord(String word_target) {
        ResultSet rs = null;
        String sqlCommand = "select * from " + table + " where word = ?";
        PreparedStatement pst = null;
        try {
            pst = connection.prepareStatement(sqlCommand);
            pst.setString(1, word_target);
            rs = pst.executeQuery();
        } catch (SQLException e) {
            System.out.println("Select error");
            e.printStackTrace();
        }
        return rs;
    }

    /**
     * xóa từ.
     */
    public void deleteWord(String word_target) {
        String sqlCommand = "delete from " + table + " where word = ?";
        PreparedStatement pst = null;
        int index = searchWord(word_target);
        Word word = new Word();
        word.word_target = list.get(index).word_target;
        word.word_explain = list.get(index).word_explain;
        try {
            pst = connection.prepareStatement(sqlCommand);
            pst.setString(1, word_target);
            if (pst.executeUpdate() > 0) {
                System.out.println("Đã xóa");
                num--;
                wordList.remove(word_target);
                list.remove(word);
                connect();
            } else {
                System.out.println("Không có từ cần xóa!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * thêm từ.
     */
    public void insert(String newWord, String meaning) {
        String sqlCommand = "insert into " + table + " value(?, ?, ?)";
        PreparedStatement pst = null;
        wordList.add(newWord);
        Word word  = new Word();
        word.word_target = newWord;
        word.word_explain = meaning;
        list.add(word);
        try {
            pst = connection.prepareStatement(sqlCommand);
            pst.setInt(1, ++num );
            pst.setString(2, newWord);
            pst.setString(3, meaning);
            if (pst.executeUpdate() > 0) {
                System.out.println("Thêm từ thành công: " + meaning);
            } else {
                System.out.println("Chưa thể thêm từ!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sửa từ.
     */
    public void fixWord(String word_target, String meaning) {
        String sqlCommand = "update " + table + " set detail = ? where word = ?";
        PreparedStatement pst = null;
        try {
            pst = connection.prepareStatement(sqlCommand);
            pst.setString(1, meaning);
            pst.setString(2, word_target);

            if (pst.executeUpdate() > 0) {
                System.out.println("update success :" + pst.executeUpdate());
            } else {
                System.out.println("update error");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * search for a word by BinarySearch
     * time complexity: O(logN)
     */
    public int searchWord( String keyword ) {
        try {
            list.sort(new SortListByWord());
            int left = 0;
            int right = list.size() - 1;
            while (left <= right) {
                int mid = left + (right - left) / 2;
                int res = list.get(mid).getWord_target().compareTo(keyword);
                if (res == 0) {
                    return mid;
                }
                if (res <= 0) {
                    left = mid + 1;

                } else {
                    right = mid - 1;
                }
            }
        } catch (NullPointerException e) {
            System.out.println("Null Exception.");
        }
        return -1;
    }

    public String showData(String word) throws SQLException {
        int tmp = searchWord(word);
        String res = list.get(tmp).getWord_explain();
        return res;
    }
}
