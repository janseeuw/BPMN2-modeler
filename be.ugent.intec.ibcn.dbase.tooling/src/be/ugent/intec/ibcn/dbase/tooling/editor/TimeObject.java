package be.ugent.intec.ibcn.dbase.tooling.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class TimeObject {

	private int i;
	private int k;
	private int depth;
	
	private List<TimeObject> timeObjects;
	private TimeObject parent;

	public TimeObject() {
		this.i = 0;
		this.k = 0;
		this.depth = 0;
		this.timeObjects = new ArrayList<TimeObject>();
		this.parent = null;
	}

	public TimeObject(TimeObject timeObject) {
		this.i = timeObject.getI();
		this.k = timeObject.getK();
		this.depth = timeObject.getDepth();
		this.timeObjects = new ArrayList<TimeObject>();
		for (TimeObject to : timeObject.getTimeObjects()) {
			TimeObject toCopy = new TimeObject(to);
			toCopy.parent = this;
			this.timeObjects.add(toCopy);
		}
	}

	public int getI() {
		return i;
	}

	public void setI(int i) {
		this.i = i;
	}

	public int getK() {
		return k;
	}

	public void setK(int k) {
		this.k = k;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public TimeObject getParent() {
		return parent;
	}

	public void setParent(TimeObject parent) {
		this.parent = parent;
	}

	public List<TimeObject> getTimeObjects() {
		return timeObjects;
	}

	public void setTimeObjects(List<TimeObject> timeObjects) {
		this.timeObjects = timeObjects;
	}

	public TimeObject addTimeObject() {
		TimeObject timeObject = new TimeObject();
		timeObject.setParent(this);
		timeObject.setDepth(this.getDepth() + 1);
		timeObject.setK(this.getTimeObjects().size());
		this.getTimeObjects().add(timeObject);

		return timeObject;
	}

	public static TimeObject clone(TimeObject timeObject) {
		int depth = timeObject.getDepth();
		Stack<Integer> pad = new Stack<Integer>();
		while (timeObject.getParent() != null) {
			pad.push(timeObject.getK());
			timeObject = timeObject.getParent();
		}
		TimeObject clone = new TimeObject(timeObject);
		while (!pad.isEmpty()) {
			clone = clone.getTimeObjects().get(pad.pop());
		}
		return clone;
	}

	public static List<TimeObject> clone(List<TimeObject> timeVector) {
		List<TimeObject> clone = new ArrayList<TimeObject>();
		for (TimeObject timeObject : timeVector) {
			clone.add(TimeObject.clone(timeObject));
		}
		return clone;
	}

	public void increment() {
		// processVector[i] = (t, X)
		// if X = []
		// processVector[i]++ = (t++, [])
		// else
		// processVector[i]++ = (t, [c,...ck++,...cm])

		if (this.getTimeObjects().isEmpty()) {
			this.setI(this.getI() + 1);
		} else {
			// verhoog kde parallele
			this.getTimeObjects().get(k).increment();
		}
	}

	// (1,[0,2]) + (1,[1,0]) => (4,[])
	public static TimeObject supremum(List<TimeObject> timeObjects) {
		/*
		 * TimeObject t = timeObjects.get(0).getParent(); int i = t.getI();
		 * for(TimeObject timeObject : t.getTimeObjects()){ i +=
		 * timeObject.getI(); } t.setI(i); t.getTimeObjects().clear(); return t;
		 */

		List<TimeObject> parents = new ArrayList<TimeObject>();
		int sum = 0;
		int numberOfPaths = 0;
		for (TimeObject timeObject : timeObjects) {
			parents.add(timeObject.getParent());
			if (timeObject.getParent().getI() > sum) {
				sum = timeObject.getParent().getI();
			}
			if (timeObject.getParent().getTimeObjects().size() > numberOfPaths) {
				numberOfPaths = timeObject.getParent().getTimeObjects().size();
			}
		}

		for (int i = 0; i < numberOfPaths; i++) {
			int max = 0;
			for (TimeObject parent : parents) {
				if (parent.getTimeObjects().get(i).getI() > max) {
					max = parent.getTimeObjects().get(i).getI();
				}
			}
			sum += max;
		}

		TimeObject t = (TimeObject) timeObjects.get(0).getParent();
		t.setI(sum);
		t.getTimeObjects().clear();
		return t;
	}
	
	// anchor
	// (0,[])(0,[2,0]) + (3,[])(1,[0,1]) => (3,[])(1,[2,1])
	public static void supremumVectorClock(List<TimeObject> receivingNodeTimeVector,
			List<TimeObject> sendingNodeTimeVector) {
		// receiving process vector krijgt telkens hoogste waarde
		for (int i = 0; i < receivingNodeTimeVector.size(); i++) {
			TimeObject receivingNodeTimeObject = receivingNodeTimeVector.get(i);
			while(receivingNodeTimeObject.getParent() != null){
				receivingNodeTimeObject = receivingNodeTimeObject.getParent();
			}
			TimeObject sendingNodeTimeObject = sendingNodeTimeVector.get(i);
			while(sendingNodeTimeObject.getParent() != null){
				sendingNodeTimeObject = sendingNodeTimeObject.getParent();
			}
			
			// change recursive
			sup(receivingNodeTimeObject, sendingNodeTimeObject);
		}
	}
	
	// helper
	public static void sup(TimeObject receivingNodeTimeObject, TimeObject sendingNodeTimeObject){
		if(receivingNodeTimeObject.lessOrEqualThan(sendingNodeTimeObject)){
			TimeObject clone = TimeObject.clone(sendingNodeTimeObject);
			clone.setParent(receivingNodeTimeObject.getParent());
			clone.setK(receivingNodeTimeObject.getK());
			clone.setDepth(receivingNodeTimeObject.getDepth());
			receivingNodeTimeObject.setI(clone.getI());
			receivingNodeTimeObject.setTimeObjects(clone.getTimeObjects());
		}else{
			// sowieso kopieren als sending langer is dan ander receiving
			int maxLength = sendingNodeTimeObject.getTimeObjects().size();
			for (int i = 0; i < maxLength; i++) {
				if (i >= receivingNodeTimeObject.getTimeObjects().size()) {
					TimeObject newTimeObject = receivingNodeTimeObject.addTimeObject();
					TimeObject clone = TimeObject.clone(sendingNodeTimeObject.getTimeObjects().get(i));
					newTimeObject.setI(clone.getI());
					newTimeObject.setTimeObjects(clone.getTimeObjects());
				} else {
					sup(receivingNodeTimeObject.getTimeObjects().get(i), sendingNodeTimeObject.getTimeObjects().get(i));
				}
			}
		}
	}

	public boolean isEqualto(TimeObject object) {
		if(this.getI() == object.getI()){
			if(this.getTimeObjects().size() != object.getTimeObjects().size()){
				return false;
			}
			boolean flag = true;
			for(int i=0; i<this.getTimeObjects().size(); i++){
				if(!this.getTimeObjects().get(i).isEqualto(object.getTimeObjects().get(i))){
					flag = false;
					break;
				}
			}
			return flag;
		}else{
			return false;
		}
	}

	public boolean lessThan(TimeObject object) {
		return this.lessOrEqualThan(object) && !this.isEqualto(object);
	}

	public boolean lessOrEqualThan(TimeObject object) {
		if (this.getI() == object.getI()) {
			if (this.getTimeObjects().size() < object.getTimeObjects().size()) {
				return true;
			} else {
				boolean flag = true;
				int maxLength = this.getTimeObjects().size() > object
						.getTimeObjects().size() ? this.getTimeObjects().size()
						: object.getTimeObjects().size();
				for (int i = 0; i < maxLength; i++) {
					if (i >= this.getTimeObjects().size()
							|| i >= object.getTimeObjects().size()) {
						TimeObject zero = new TimeObject(); // compare to zero
						if (i >= this.getTimeObjects().size()) {
							if (!zero.lessOrEqualThan(object.getTimeObjects()
									.get(i))) {
								flag = false;
								break;
							}
						} else {
							if (!this.getTimeObjects().get(i)
									.lessOrEqualThan(zero)) {
								flag = false;
								break;
							}
						}
					} else {
						if (!this
								.getTimeObjects()
								.get(i)
								.lessOrEqualThan(object.getTimeObjects().get(i))) {
							flag = false;
							break;
						}
					}

				}
				return flag;
			}
		} else {
			return this.getI() < object.getI();
		}
	}

	public boolean isParallel(TimeObject object) {
		if (this.getI() != object.getI()) {
			return false;
		}
		int maxLength = this.getTimeObjects().size() > object.getTimeObjects()
				.size() ? this.getTimeObjects().size() : object
				.getTimeObjects().size();
		for (int i = 0; i < maxLength; i++) {
			for (int j = 0; j < maxLength; j++) {
				if(i != j){
					if (i >= this.getTimeObjects().size()
							|| i >= object.getTimeObjects().size()) {
						TimeObject zero = new TimeObject(); // compare to zero
						if (i >= this.getTimeObjects().size()) {
							if (zero
									.lessThan(object.getTimeObjects().get(i))
									&& object.getTimeObjects().get(j)
											.lessThan(zero)) {
								return true;
							}
						} else {
							if (this.getTimeObjects().get(i)
									.lessThan(zero)
									&& zero
											.lessThan(this.getTimeObjects().get(j))) {
								return true;
							}
						}
					} else {
						if (this.getTimeObjects().get(i)
								.lessThan(object.getTimeObjects().get(i))
								&& object.getTimeObjects().get(j)
										.lessThan(this.getTimeObjects().get(j))) {
							return true;
						}
					}
				}
				
			}
		}

		return false;
	}

	@Override
	public String toString() {
		String a = "(" + this.getI() + ",[";
		for (TimeObject t : this.getTimeObjects()) {
			a += t.toString();
		}
		a += "])";
		return a;
	}
	
	public static String printTimeVector(List<TimeObject> timeVector){
		String output = "";
		for(TimeObject timeObject : timeVector){
			while (timeObject.getParent() != null) {
				timeObject = timeObject.getParent();
			}
			output += timeObject.toString();
		}
		return output;
	}

}
