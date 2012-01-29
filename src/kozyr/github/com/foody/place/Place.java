package kozyr.github.com.foody.place;

public class Place {
	private long rowId;
	protected String name;
	protected String review;
	protected int rating;
	
	public Place(long rowId, String name, String review, int rating) {
		this.rowId = rowId;
		this.review = review;
		this.name = name;
		this.rating = rating;
	}
	
	public String getReview() {
		return review;
	}
	public void setReview(String review) {
		this.review = review;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getRating() {
		return rating;
	}
	public void setRating(int rating) {
		this.rating = rating;
	}
	
	public long getRowId() {
		return rowId;
	}
}
