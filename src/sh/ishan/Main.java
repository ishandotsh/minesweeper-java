package sh.ishan;

import java.util.*;

public class Main {
    private static char safe, mine, mine_mark, explored;
    private static int size;
    private static boolean won;
    private static ArrayList<Integer> marked, mine_locations; // initiated in method addMines

    private static void generateMines(char[][] state, int n){
        // generate random mine locations

        Random rand = new Random(42);
        mine_locations = new ArrayList<>(n);
        for(int i = 0; i < n; i++) mine_locations.add(-1);
        marked = new ArrayList<>();

        for(int i = 0; i < n;){
            int new_mine = rand.nextInt(size * size);
            if(mine_locations.contains(new_mine)) continue;
            mine_locations.set(i, new_mine);
            i++;
        }

        // add mines to board
        for(int location : mine_locations){
            int row = location / size;
            int col = location % size;
            state[row][col] = mine;
        }
    }

    private static char[][] addNeighbourMines(char[][] state) {
        char[][] newState = new char[size][size];
        for(int i = 0; i < size; i++){
            System.arraycopy(state[i], 0, newState[i], 0, size);
        }
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                if(state[i][j] != mine) {
                    int mine_count = 0;
                    for(int k = -1; k <= 1; k++){
                        for(int l = -1; l <= 1; l++){
                            if(onBoard(i+k, j+l)){
                                if(isMine(state, i+k, j+l))
                                    mine_count++;
                            }
                        }
                    }
                    if(state[i][j] == mine) mine_count--;
                    // using "+ '0'" converts int to its char version
                    if(mine_count > 0) newState[i][j] = (char)(mine_count+'0');
                }
            }
        }
        return newState;
    }

    private static boolean onBoard(int row, int col){
        return (row >= 0 && row < size) && (col >= 0 && col < size);
    }

    private static boolean isMine(char[][] state, int row, int col){
        return state[row][col] == mine;
    }

    private static char[][] generateNewBoard(){
        char[][] state = new char[size][size];
        Arrays.stream(state).forEach(row -> Arrays.fill(row, safe));
        return state;
    }

    private static void printBoard(char[][] state){
        System.out.print(" │");
        for(int i = 1; i <= size; i++) System.out.print(i);
        System.out.println("|\n—│—————————│");
        for(int i = 0; i < size; i++){
            System.out.print(i+1 + "│");
            for(int j = 0; j < size; j++){
                System.out.print(state[i][j]);
            }
            System.out.println("│");
        }
        System.out.println("—│—————————│");
    }

    private static boolean isTerminal(char[][] state){
        Collections.sort(mine_locations);
        Collections.sort(marked);
        if (!mine_locations.equals(marked)) {
            for (int i = 0; i < size * size; i++) {
                if (state[i / size][i % size] == safe && !mine_locations.contains(i)) {
                    return false;
                }
            }
        }
        won = true;
        return true;
    }

    private static void revealMines(char[][] state){
        for(int location : mine_locations){
            int row = location / size;
            int col = location % size;
            state[row][col] = mine;
        }
    }

    private static void exploreCell(char[][] state, char[][] ref, int row, int col){
        if(ref[row][col] == safe){
            if(ref[row][col] == mine_mark) marked.remove(Integer.valueOf(row*size + col));
            state[row][col] = explored;
            revealNeighbourhood(state, ref, row, col);
        } else {
            state[row][col] = ref[row][col];
        }
    }

    private static void revealNeighbourhood(char[][] state, char[][] ref, int row, int col){
        int curRow, curCol;
        for(int i = -1; i <= 1; i++){
            for(int j = -1; j <= 1; j++){
                if(i == 0 && j == 0) continue;
                curRow = row+i;
                curCol = col+j;
                if(onBoard(curRow, curCol)){
                    if(state[curRow][curCol] == safe){
                        if(ref[curRow][curCol] != mine && ref[curRow][curCol] != safe){
                            state[curRow][curCol] = ref[curRow][curCol];
                        } else if (ref[curRow][curCol] == safe) {
                            exploreCell(state, ref, curRow, curCol);
                        }
                    }
                    else if(state[curRow][curCol] == mine_mark){
                        marked.remove(Integer.valueOf(curRow * size + curCol));
                        if(ref[curRow][curCol] == safe) {
                            state[curRow][curCol] = explored;
                            exploreCell(state, ref, curRow, curCol);
                        }
                        else state[curRow][curCol] = ref[curRow][curCol];
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        safe = '.';
        mine = 'X';
        mine_mark = '*';
        explored = '/';
        size = 9;

        won = false;

        Scanner stdin = new Scanner(System.in);
        System.out.print("How many mines do you want on the field? ");
        int num_mines = stdin.nextInt();

        // initialize board as safe
        char[][] state = generateNewBoard();

        // Generate and add mines
        generateMines(state, num_mines);

        // Get neighbouring mine count
        state = addNeighbourMines(state);
        char[][] outputState = generateNewBoard();

        while (true) {
            // output board
            printBoard(outputState);
            if(isTerminal(outputState)) break;

            System.out.println("Set/unset mines marks or claim a cell as free:");
            int col = stdin.nextInt() - 1;
            int row = stdin.nextInt() - 1;
            String command = stdin.next();
            if(onBoard(row, col)){
                if("free".equals(command)){
                    if(isMine(state, row, col)){
                        revealMines(outputState);
                        printBoard(outputState);
                        System.out.println("You stepped on a mine and failed!");
                        break;
                    }
                    exploreCell(outputState, state, row, col);

                } else if ("mine".equals(command)){
                    if (outputState[row][col] == safe) {
                        outputState[row][col] = mine_mark;
                        marked.add((Integer)(row * size + col));
                    } else if (outputState[row][col] == mine_mark) {
                        outputState[row][col] = safe;
                        marked.remove(Integer.valueOf(row * size + col));
                    } else
                        System.out.println("There is a number here!");
                }
            }
        }
        if(won)
            System.out.println("Congratulations! You found all mines!");
    }
}

