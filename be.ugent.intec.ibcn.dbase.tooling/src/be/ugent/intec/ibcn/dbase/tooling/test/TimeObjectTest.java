package be.ugent.intec.ibcn.dbase.tooling.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import be.ugent.intec.ibcn.dbase.tooling.editor.TimeObject;

public class TimeObjectTest {

	public static void main(String[] args) {
		//simpleTest();
		//complexTest();
		
		//parallelTest();
		
		//complexSupremumTest();
	}
	
	public static void simpleSupremumTest(){
		
		TimeObject t1 = new TimeObject();
		TimeObject t1a = t1.addTimeObject();
		t1a.increment();
		TimeObject t1b = t1.addTimeObject();
		List<TimeObject> receivingNodeTimeVector = new ArrayList<TimeObject>();
		receivingNodeTimeVector.add(t1);
		System.out.print(TimeObject.printTimeVector(receivingNodeTimeVector));
		
		TimeObject t2 = new TimeObject();
		t2.setI(7);
		List<TimeObject> sendingNodeTimeVector = new ArrayList<TimeObject>();
		sendingNodeTimeVector.add(t2);
		System.out.print(" + " + TimeObject.printTimeVector(sendingNodeTimeVector));

		
		TimeObject.supremumVectorClock(receivingNodeTimeVector, sendingNodeTimeVector);
		
		System.out.print(" => " + TimeObject.printTimeVector(receivingNodeTimeVector));
	}
	
	public static void complexSupremumTest(){
		// (0,[])(1,[2,0]) + (3,[])(1,[0,1]) => (3,[])(1,[2,1])
		
		// (0,[])(1,[2,0])
		TimeObject t1a = new TimeObject();
		TimeObject t1b = new TimeObject();
		t1b.increment();
		TimeObject t1ba = t1b.addTimeObject();
		t1ba.increment();
		t1ba.increment();
		TimeObject t1bb = t1b.addTimeObject();
		
		List<TimeObject> receivingNodeTimeVector = new ArrayList<TimeObject>();
		receivingNodeTimeVector.add(t1a);
		receivingNodeTimeVector.add(t1b);
		System.out.print(TimeObject.printTimeVector(receivingNodeTimeVector));

		
		// (3,[])(1,[0,1])
		TimeObject t2a = new TimeObject();
		t2a.setI(3);
		
		TimeObject t2b = new TimeObject();
		t2b.increment();
		TimeObject t2ba = t2b.addTimeObject();
		TimeObject t2bb = t2b.addTimeObject();
		t2bb.increment();
		
		List<TimeObject> sendingNodeTimeVector = new ArrayList<TimeObject>();
		sendingNodeTimeVector.add(t2a);
		sendingNodeTimeVector.add(t2b);
		System.out.print(" + " + TimeObject.printTimeVector(sendingNodeTimeVector));

		
		TimeObject.supremumVectorClock(receivingNodeTimeVector, sendingNodeTimeVector);
		
		System.out.print(" => " + TimeObject.printTimeVector(receivingNodeTimeVector));
	}
	
	public static void parallelTest(){
		// (1,[2,0,4]) || (1,[1,0,1])
		TimeObject t1 = new TimeObject();
		t1.increment();
		TimeObject t1a = t1.addTimeObject();
		t1a.increment();
		t1a.increment();
		TimeObject t1b = t1.addTimeObject();
		TimeObject t1c = t1.addTimeObject();
		t1c.increment();
		t1c.increment();
		t1c.increment();
		t1c.increment();
		
		TimeObject t2 = new TimeObject();
		t2.increment();
		TimeObject t2a = t2.addTimeObject();
		t2a.increment();
		TimeObject t2b = t2.addTimeObject();
		t2b.increment();
		TimeObject t2c = t2.addTimeObject();
		
		System.out.println(t1.isParallel(t2));
	}
	
	public static void nonParallelTest(){
		// (0,[1,0]) || (0,[1,1])
		TimeObject t1 = new TimeObject();
		TimeObject t1a = t1.addTimeObject();
		TimeObject t1b = t1.addTimeObject();
		t1b.increment();
		
		TimeObject t2 = new TimeObject();
		TimeObject t2a = t2.addTimeObject();
		t2a.increment();
		TimeObject t2b = t2.addTimeObject();
		t2b.increment();
		
		System.out.println(t1.isParallel(t2));
	}
	
	public static void simpleTest(){
		// (0, [])
		TimeObject t1 = new TimeObject();
		
		// increment
		// (1, [])
		t1.increment();
		
		// fork
		// (1,[(0,[]),(0,[])])
		// (1,[(0,[]),(0,[])])
		TimeObject t1a = t1.addTimeObject();
		TimeObject t1b = t1.addTimeObject();
		
		// clone t1a, t1b
		t1a = TimeObject.clone(t1a);
		t1b = TimeObject.clone(t1b);
		
		// increment
		// (1,[(1,[]),(0,[])])
		// (1,[(0,[]),(1,[])])
		t1a.increment();
		t1b.increment();
		
		// join
		// (3,[])
		List<TimeObject> timeObjects = new ArrayList<TimeObject>();
		timeObjects.add(t1a);
		timeObjects.add(t1b);
		t1 = TimeObject.supremum(timeObjects);
		
		TimeObject t = new TimeObject();
		t.setI(3);
		
		System.out.println(t1 + " " + t + " " +t1.isEqualto(t));
	}
	
	public static void complexTest(){
		// (0, [])
		TimeObject t1 = new TimeObject();
		
		// fork
		// (0,[(0,[]),(0,[])])
		TimeObject t1a = t1.addTimeObject();
		TimeObject t1b = t1.addTimeObject();
		
		// fork
		// (0,[(0,[(0,[]),(0,[])]),(0,[])])
		TimeObject t1aa = t1a.addTimeObject();
		TimeObject t1ab = t1a.addTimeObject();
		
		// increment
		// (0,[(0,[(1,[]),(2,[])]),(1,[])])
		t1aa.increment();
		t1ab.increment();
		t1ab.increment();
		t1b.increment();
		
		// join
		List<TimeObject> timeObjects = new ArrayList<TimeObject>();
		timeObjects.add(t1aa);
		timeObjects.add(t1ab);
		t1a = TimeObject.supremum(timeObjects);
		
		// join
		List<TimeObject> timeObjects2 = new ArrayList<TimeObject>();
		timeObjects2.add(t1a);
		timeObjects2.add(t1b);
		t1 = TimeObject.supremum(timeObjects2);
		
		TimeObject t = new TimeObject();
		t.setI(4);
		
		System.out.println(t1 + " " + t + " " + t1.isEqualto(t));
	}

}
