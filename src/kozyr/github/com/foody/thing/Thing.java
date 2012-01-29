package kozyr.github.com.foody.thing;

public class Thing {
	private long rowId;
	protected String name;
	protected String review;
	protected int rating;
	protected String lastModified;
	protected String createdOn;
	protected long placeId;
	
	public Thing(long rowId, String name, String review, int rating, String lastModified, String createdOn, long placeId) {
		super();
		this.rowId = rowId;
		this.name = name;
		this.review = review;
		this.rating = rating;
		this.lastModified = lastModified;
		this.createdOn = createdOn;
		this.placeId = placeId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getReview() {
		return review;
	}

	public void setReview(String review) {
		this.review = review;
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

	public long getPlaceId() {
		return placeId;
	}
	
	public String getLastModified() {
		return lastModified;
	}
	
	public String getCreatedOn() {
		return createdOn;
	}
	
}
