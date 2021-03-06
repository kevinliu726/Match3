package model;

import java.util.*;
import controller.*;
import model.Gem.Colour;
import view.GameView;

public class Board {
    private final int boardSize = 10;
    private Gem grid[][] = new Gem[boardSize][boardSize];
    private String imagePath[][] = new String[boardSize][boardSize];
    private Gem selected;
    private ArrayList<Gem> clearList = new ArrayList<>();
    private Queue<Gem> effectList = new LinkedList<>();
    private boolean firstPressed = true;
    private GameController gameController;

    public Board(GameController gameController) {
        this.gameController = gameController;
        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                grid[x][y] = new Basic();
                grid[x][y].setMyXY(x, y);
            }
        }

        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                while (checkMatch(x, y)) {
                    grid[x][y].setColour();
                }
                imagePath[x][y] = grid[x][y].getImagePath();
            }
        }
    }

    public boolean getSetButtonBorder(int x, int y) {
        return grid[x][y].getSetButtonBorder();
    }

    public boolean getRemoveFlag(int x, int y) {
        return grid[x][y].getRemoveFlag();
    }

    public Gem getGem(int x, int y) {
        return grid[x][y];
    }

    public String getGridImagePath(int x, int y) {
        return imagePath[x][y];
    }

    public void performClick(Gem source) {
        if(source.getColour() == Colour.CROSS){
            selected = source;
            selected.setButtonBorder(true);
            firstPressed = true;
            this.gameController.performCross(selected);
        }
        else if (firstPressed) {
            selected = source;
            selected.setButtonBorder(true);
            firstPressed = false;
        } else {
            selected.setButtonBorder(false);
            if (selected.isNextTo(source)) {
                swap(selected, source);
                this.gameController.checkIfMatch(selected, source);
                firstPressed = true;
            } else if (source == selected) {
                firstPressed = true;
            } else {
                selected = source;
                selected.setButtonBorder(true);
            }
        }
    }

    public void clearGrid(Gem a, Gem b) {
        try {
            Thread.sleep(200); // delays 1 second
        } catch (InterruptedException e) {
        }
        boolean aMatch = clearMatch(a.getMyX(), a.getMyY());
        boolean bMatch = clearMatch(b.getMyX(), b.getMyY());
        if (!aMatch && !bMatch) {
            swap(a, b);
        }
        gameController.repaintBoard(1);
        try {
            Thread.sleep(200); // delays 1 second
        } catch (InterruptedException e) {
        }
    }
    public void dealMyTimer(){
        MyTimer timer = new MyTimer(gameController);
    }
    
    public void clearCross(Gem a){
        try {
            Thread.sleep(200); // delays 1 second
        } catch (InterruptedException e) {
        }
        int x = a.getMyX();
        int y = a.getMyY();
        clearList.add(a);
        for(int i = 0;i < boardSize;i++){
            if(i != x){
                clearList.add(grid[i][y]);
            }
            if(i != y){
                clearList.add(grid[x][i]);
            }
        }
        for(Gem gem: clearList){
            gem.setRemoveFlag(true);
        }
        GameView.points += clearList.size() * 100;
        gameController.updatePoint();
        gameController.repaintBoard(1);
        try {
            Thread.sleep(200); // delays 1 second
        } catch (InterruptedException e) {
        }
    }

    public void refillGrid() {
        boolean cross = false;
        Collections.sort(clearList);
        if(clearList.size() >= 5 && clearList.size() < 19) cross = true;
        for (Gem gem : clearList) {
            gem.setRemoveFlag(false);
            int x = gem.getMyX();
            for(int i = gem.getMyY();i > 0;i--){
                grid[x][i].setColour(grid[x][i-1].getColour());
                imagePath[x][i] = grid[x][i].getImagePath();
            }
            if(cross){
                grid[x][0].setColour(Colour.CROSS);
                cross = false;
            }
            else grid[x][0].setColour();
            imagePath[x][0] = grid[x][0].getImagePath();
            
        }
        clearList.removeAll(clearList);
        gameController.repaintBoard(1);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        autoCrush();
    }

    private void autoCrush(){
        for(int i = 0;i < boardSize;i++){
            for(int j = 0;j < boardSize;j++){
                if(checkGem(i, j)){
                    clearMatch(i, j);
                }
            }
        }
        if(clearList.isEmpty()){
            return;
        }
        dealMyTimer();
        try {
            Thread.sleep(700); // delays 1 second
        } catch (InterruptedException e) {
        }
        gameController.repaintBoard(-9);
        try {
            Thread.sleep(200); // delays 1 second
        } catch (InterruptedException e) {
        }
        refillGrid();
    }

    private boolean checkGem(int x, int y){
        if(clearList.indexOf(grid[x][y]) != -1) return false;
        return true;
    }

    private boolean clearMatch(int x, int y) {
        Colour colour = grid[x][y].getColour();
        //System.out.println(colour);
        int dx[] = { 0, 1, 0, -1 };
        int dy[] = { -1, 0, 1, 0 };
        int cnt[] = { 0, 0, 0, 0 };
        ArrayList<ArrayList<Gem>> gemList = new ArrayList<ArrayList<Gem>>(4);
        for (int i = 0; i < 4; i++) {
            int newX = x + dx[i], newY = y + dy[i];
            ArrayList<Gem> tempList = new ArrayList<Gem>();
            while (isValid(newX, newY)) {
                if (grid[newX][newY].getColour() == colour) {
                    cnt[i]++;
                    tempList.add(grid[newX][newY]);
                    newX += dx[i];
                    newY += dy[i];
                } else {
                    break;
                }
            }
            gemList.add(tempList);
        }
        ArrayList<Gem> clearListTmp = new ArrayList<>();
        Queue<Gem> effectListTmp = new LinkedList<>();
        clearListTmp.add(grid[x][y]);
        if (cnt[0] + cnt[2] >= 2) {
            for(Gem g: gemList.get(0)){
                if(clearListTmp.indexOf(g) == -1){
                    clearListTmp.add(g);
                }
            }
            for(Gem g: gemList.get(2)){
                if(clearListTmp.indexOf(g) == -1){
                    clearListTmp.add(g);
                }
            }
        } 
        if (cnt[1] + cnt[3] >= 2) {
            for(Gem g: gemList.get(1)){
                if(clearListTmp.indexOf(g) == -1){
                    clearListTmp.add(g);
                }
            }
            for(Gem g: gemList.get(3)){
                if(clearListTmp.indexOf(g) == -1){
                    clearListTmp.add(g);
                }
            }
        }
        if (clearListTmp.size() > 1) {
            
            for (Gem gem : clearListTmp) {
                gem.setRemoveFlag(true);
            }
            GameView.points += clearListTmp.size() * 100;
            gameController.updatePoint();
            clearList.addAll(clearListTmp);
            //effectList.addAll(effectListTmp);
        } else {
            return false;
        }
        return true;
    }

    public boolean checkMatch(int x, int y) {
        Colour colour = grid[x][y].getColour();
        // System.out.println(x + " " + y + " " + colour);
        int dx[] = { 0, 1, 0, -1 };
        int dy[] = { -1, 0, 1, 0 };
        int cnt[] = { 0, 0, 0, 0 };
        for (int i = 0; i < 4; i++) {
            int newX = x + dx[i], newY = y + dy[i];
            while (isValid(newX, newY)) {
                // System.out.println(" " + newX + " " + newY + " " +
                // grid[newX][newY].getGemColour());
                if (grid[newX][newY].getColour() == colour) {
                    cnt[i]++;
                    newX += dx[i];
                    newY += dy[i];
                } else {
                    break;
                }
            }
        }
        if ((cnt[0] + cnt[2] >= 2) || (cnt[1] + cnt[3] >= 2) || (cnt[0] + cnt[1] >= 4) || (cnt[0] + cnt[3] >= 4)
                || (cnt[1] + cnt[2] >= 4) || (cnt[2] + cnt[3] >= 4)) {
            return true;
        }

        return false;
    }

    private boolean isValid(int x, int y) {
        return (x >= 0 && x < this.boardSize && y >= 0 && y < this.boardSize);
    }

    private void swap(Gem first, Gem second) {
        int fX = first.getMyX(), fY = first.getMyY(), sX = second.getMyX(), sY = second.getMyY();
        String temp = imagePath[fX][fY];
        imagePath[fX][fY] = imagePath[sX][sY];
        imagePath[sX][sY] = temp;
        grid[fX][fY] = second;
        grid[sX][sY] = first;
        grid[fX][fY].setMyXY(fX, fY);
        grid[sX][sY].setMyXY(sX, sY);
    }
}
