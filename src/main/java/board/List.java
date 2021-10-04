package board;

import org.json.JSONObject;

public class List {
    private int id, board_id;
    private String title, tasks;
    private JSONObject tasksJSON;

    public List(int board_id, String title, String tasks) {
        this.board_id = board_id;
        this.title = title;
        this.tasks = tasks;
    }

    public List(int id, int board_id, String title, String tasks) {
        this.id = id;
        this.board_id = board_id;
        this.title = title;
        this.tasks = tasks;
    }
}
