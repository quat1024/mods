package uploaderthing.rinth;

public class ModrinthVersionQueryResponse {
	String date_published;
	String version_number;
	String id;
	//and way more fields, https://docs.modrinth.com/api/operations/getprojectversions/
	
	@Override
	public String toString() {
		return "ModrinthVersionQueryResponse{date_published='%s', version_number='%s', id='%s'}".formatted(date_published, version_number, id);
	}
}
