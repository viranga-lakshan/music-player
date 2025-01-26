package mymusicplayer;

public class PlayList {
    private Song head;
    private Song current;
    private int size;
    
    public PlayList() {
        head = null;
        current = null;
        size = 0;
    }
    
    public void addSong(String title, String path, String albumArtPath) {
        Song newSong = new Song(title, path, albumArtPath);
        if (head == null) {
            head = newSong;
            current = head;
        } else {
            Song temp = head;
            while (temp.getNext() != null) {
                temp = temp.getNext();
            }
            temp.setNext(newSong);
            newSong.setPrevious(temp);
        }
        size++;
    }
    
    public Song getCurrentSong() {
        return current;
    }
    
    public Song nextSong() {
        if (current != null && current.getNext() != null) {
            current = current.getNext();
        }
        return current;
    }
    
    public Song previousSong() {
        if (current != null && current.getPrevious() != null) {
            current = current.getPrevious();
        }
        return current;
    }
    
    public int getSize() {
        return size;
    }
} 