/**
 * Add supported files here
 * @author Yafim Vodkov 308973882  Nir Tahan 305181166
 *
 */
public enum SupportedFiles {

	/** Files */
	html{
		@Override	
		public String getContentType(){return "txt/html";}
		},
	//TODO: Bonus here... maybe js and txt files?
		txt{
			@Override
			public String getContentType(){return "txt/txt";}
		},
	
	/** Image files supported */
	bmp{
		@Override	
		public String getContentType(){return "image/bmp";}
		},
	gif{
		@Override	
		public String getContentType(){return "image/gif";}
		},
	png{
		@Override	
		public String getContentType(){return "image/png";}
		},
	jpg{
		@Override	
		public String getContentType(){return "image/jpg";}
		};
		

	public abstract String getContentType();

}
