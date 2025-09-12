package uploaderthing;

import uploaderthing.meta.ModMeta;
import uploaderthing.meta.PublishableProject;

import java.nio.file.Path;
import java.util.List;

public class UploadBundle {
	public PublishableProject proj;
	public ModMeta meta;
	public List<String> changelog;
	public Path jar;
}
