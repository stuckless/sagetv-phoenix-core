package sagex.phoenix.vfs.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import sagex.phoenix.vfs.IAlbumInfo;
import sagex.phoenix.vfs.IMediaFile;

public class AlbumInfo implements IAlbumInfo, Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String art;
    private String artist;
    private String genre;
    private String year;
    private List<IMediaFile> tracks = new ArrayList<IMediaFile>();
    
    public AlbumInfo() {
    }

    public boolean hasArt() {
        return getArt()!=null;
    }

    /**
     * @return the art
     */
    public String getArt() {
        return art;
    }

    /**
     * @param art the art to set
     */
    public void setArt(String art) {
        this.art = art;
    }

    /**
     * @return the artist
     */
    public String getArtist() {
        return artist;
    }

    /**
     * @param artist the artist to set
     */
    public void setArtist(String artist) {
        this.artist = artist;
    }

    /**
     * @return the genre
     */
    public String getGenre() {
        return genre;
    }

    /**
     * @param genre the genre to set
     */
    public void setGenre(String genre) {
        this.genre = genre;
    }

    /**
     * @return the year
     */
    public String getYear() {
        return year;
    }

    /**
     * @param year the year to set
     */
    public void setYear(String year) {
        this.year = year;
    }

    /**
     * @return the tracks
     */
    public List<IMediaFile> getTracks() {
        return tracks;
    }

    /**
     * @param tracks the tracks to set
     */
    public void setTracks(List<IMediaFile> tracks) {
        this.tracks = tracks;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
