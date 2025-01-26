package mymusicplayer;

public class Song {
    private String title;
    private String path;
    private String albumArtPath;
    private Song next;
    private Song previous;
    
    public Song(String title, String path, String albumArtPath) {
        this.title = title;
        this.path = path;
        this.albumArtPath = albumArtPath;
        this.next = null;
        this.previous = null;
    }
    
    public String getTitle() { 
        return title; 
    }
    
    public String getPath() { 
        return path; 
    }
    
    public String getAlbumArtPath() {
        return albumArtPath;
    }
    
    public Song getNext() { 
        return next; 
    }
    
    public Song getPrevious() { 
        return previous; 
    }
    
    public void setNext(Song next) { 
        this.next = next; 
    }
    
    public void setPrevious(Song previous) { 
        this.previous = previous; 
    }
} 