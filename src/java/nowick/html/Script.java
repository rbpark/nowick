package nowick.html;

public class Script extends DomElement {
	public Script() {
		super.setAttribute("type", "text/javascript");
	}
	
	protected void add(DomElement element) {
	}
	
	@Override
	public String getTag() {
		return "script";
	}

	public void setSource(String src) {
		setAttribute("src", src);
	}
}
