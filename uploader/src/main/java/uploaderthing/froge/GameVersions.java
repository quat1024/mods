package uploaderthing.froge;

public class GameVersions {
	//id: 158,
	//    gameVersionTypeID: 42,
	//    name: "1.8.4",
	//    slug: "1-8-4"
	int id;
	int gameVersionTypeID;
	String name;
	String slug;
	
	@Override
	public String toString() {
		return name + " id(" + id + ") gvti(" + gameVersionTypeID + ")";
	}
}
