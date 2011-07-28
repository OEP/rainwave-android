package cc.rainwave.android.api.types;

public class Song {
	public String song_title;
	public Artist artists[];
	public String album_art;
	public String album_name;
	
	public String collapseArtists() {
	    return collapseArtists(", ", " & ");
	}
	
	public String collapseArtists(String comma, String and) {
	    switch(artists.length) {
	        case 0: return "???";
	        case 1: return artists[0].artist_name;
	        case 2: return artists[0].artist_name + " " + and + " " + artists[1].artist_name;
	        default:
	            StringBuilder sb = new StringBuilder();
	            for(int i = 0; i < artists.length; i++) {
	                sb.append(artists[i].artist_name);
	                
	                if(i < artists.length - 2) {
	                    sb.append(comma);
	                }
	                else if(i == artists.length - 2) {
	                    sb.append(and);
	                }
	            }
	            return sb.toString();
	    }
	}
}
