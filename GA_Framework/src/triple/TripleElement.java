package triple;

/**
 * An object of this class represents a element that could be inserted in a prohibited list
 * for MAXQBFPT problem. An element is simply a variable of the instance.
 * 
 * @author Felipe de Carvalho Pereira [felipe.pereira@students.ic.unicamp.br]

 */
public class TripleElement {

    public final Integer index; //The index of the variable
    public Boolean selected; //If the element is already selected in the partial solution
    public Boolean available; // If the element is available to be inserted into the partial solution

	public TripleElement(int index) {
        this.index = index;
        this.selected = false;
        this.available = true;
    }
		
	//Getters and Setters
	
	public Integer getIndex() {
		return index;
	}
	
	public Boolean getSelected() {
		return selected;
	}

	public Boolean getAvailable() {
		return available;
	}

	public void setAvailable(Boolean available) {
		this.available = available;
	}
	
	public void setSelected(Boolean selected) {
		this.selected = selected;
	}
}
