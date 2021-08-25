package octree;

import static org.junit.jupiter.api.Assertions.*;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import javafx.geometry.Point3D;

import org.junit.jupiter.api.Test;

class OctreeTest {
	static Random random = new Random();
	static final int NUM_ENTRIES = 100_000;

	/**
	 * Tests the efficiency of Octree operations compared to TreeMap and HashMap operations. This
	 * test does not include any assertions; results will be printed and can be compared manually.
	 * Note that this is much slower than the other tests.
	 */
//	@Test
	void testEfficiency() {
		int numPoints = 5_000_000; // the number of points to be used in the maps
		int numNearest = 5; // the number of points to find with getNearestEntries()

		// create a List of random points
		List<Point3D> points = new ArrayList<>(numPoints);
		for (int i = 0; i < numPoints; i++) {
			points.add(new Point3D(random.nextLong(), random.nextLong(), random.nextLong()));
		}

		// initialize the maps
		Octree<Point3D> octree = new Octree<>();
		TreeMap<Point3D, Point3D> treemap = new TreeMap<>(Comparator.comparing(Point3D::getX)
				.thenComparing(Point3D::getY).thenComparing(Point3D::getZ));
		HashMap<Point3D, Point3D> hashmap = new HashMap<>();

		// time the put(), get(), getNearestEntry(), getNearestEntries(), iterate, and remove()
		// operations for the Octree
		long start = System.currentTimeMillis();
		for (Point3D point : points) {
			octree.put(point, point);
		}
		long stop = System.currentTimeMillis();
		long octreePutTime = stop - start;
		start = stop;
		for (Point3D point : points) {
			octree.get(point);
		}
		stop = System.currentTimeMillis();
		long octreeGetTime = stop - start;
		start = stop;
		for (int i = 0; i < numPoints; i++) {
			octree.getNearestEntry(
					new Point3D(random.nextLong(), random.nextLong(), random.nextLong()));
		}
		stop = System.currentTimeMillis();
		long octreeGetNearestTime = stop - start;
		start = stop;
		for (int i = 0; i < numPoints; i++) {
			octree.getNearestEntries(
					new Point3D(random.nextLong(), random.nextLong(), random.nextLong()),
					numNearest);
		}
		stop = System.currentTimeMillis();
		long octreeGetNearest10Time = stop - start;
		start = stop;
		Iterator<Entry<Point3D, Point3D>> iterator = octree.entrySet().iterator();
		while (iterator.hasNext()) {
			iterator.next();
		}
		stop = System.currentTimeMillis();
		long octreeIterateTime = stop - start;
		start = stop;
		for (Point3D point : points) {
			octree.remove(point);
		}
		stop = System.currentTimeMillis();
		long octreeRemoveTime = stop - start;

		// time the put(), get(), iterate, and remove() operations for the TreeMap
		start = stop;
		for (Point3D point : points) {
			treemap.put(point, point);
		}
		stop = System.currentTimeMillis();
		long treemapPutTime = stop - start;
		start = stop;
		for (Point3D point : points) {
			treemap.get(point);
		}
		stop = System.currentTimeMillis();
		long treemapGetTime = stop - start;
		start = stop;
		iterator = treemap.entrySet().iterator();
		while (iterator.hasNext()) {
			iterator.next();
		}
		stop = System.currentTimeMillis();
		long treemapIterateTime = stop - start;
		start = stop;
		for (Point3D point : points) {
			treemap.remove(point);
		}
		stop = System.currentTimeMillis();
		long treemapRemoveTime = stop - start;

		// time the put(), get(), iterate, and remove() operations for the HashMap
		start = stop;
		for (Point3D point : points) {
			hashmap.put(point, point);
		}
		stop = System.currentTimeMillis();
		long hashmapPutTime = stop - start;
		start = stop;
		for (Point3D point : points) {
			hashmap.get(point);
		}
		stop = System.currentTimeMillis();
		long hashmapGetTime = stop - start;
		start = stop;
		iterator = hashmap.entrySet().iterator();
		while (iterator.hasNext()) {
			iterator.next();
		}
		stop = System.currentTimeMillis();
		long hashmapIterateTime = stop - start;
		start = stop;
		for (Point3D point : points) {
			hashmap.remove(point);
		}
		stop = System.currentTimeMillis();
		long hashmapRemoveTime = stop - start;

		// print the results
		System.out.println("Number of elements: " + numPoints);
		System.out.println();
		System.out.println("Octree:");
		System.out.println("Time to put all elements: " + octreePutTime + "ms");
		System.out.println("Time to get all elements: " + octreeGetTime + "ms");
		System.out.println("Time to get nearest entry for " + numPoints + " points: "
				+ octreeGetNearestTime + "ms");
		System.out.println("Time to get " + numNearest + " nearest entries for " + numPoints
				+ " points: " + octreeGetNearest10Time + "ms");
		System.out.println("Time to iterate through all elements: " + octreeIterateTime + "ms");
		System.out.println("Time to remove all elements: " + octreeRemoveTime + "ms");
		System.out.println();
		System.out.println("TreeMap:");
		System.out.println("Time to put all elements: " + treemapPutTime + "ms");
		System.out.println("Time to get all elements: " + treemapGetTime + "ms");
		System.out.println("Time to iterate through all elements: " + treemapIterateTime + "ms");
		System.out.println("Time to remove all elements: " + treemapRemoveTime + "ms");
		System.out.println();
		System.out.println("HashMap:");
		System.out.println("Time to put all elements: " + hashmapPutTime + "ms");
		System.out.println("Time to get all elements: " + hashmapGetTime + "ms");
		System.out.println("Time to iterate through all elements: " + hashmapIterateTime + "ms");
		System.out.println("Time to remove all elements: " + hashmapRemoveTime + "ms");
	}

	/**
	 * Tests the constructors.
	 */
	@Test
	void testConstructors() {
		Map<Point3D, Object> map = new HashMap<>(4);
		map.put(new Point3D(0, 0, 0), "1");
		map.put(new Point3D(1, 0, 0), "2");
		map.put(new Point3D(0, 1, 0), "3");
		map.put(new Point3D(0, 0, 1), "4");

		Octree.DistFunction distFunction = (x, y, z) -> x + y + z;

		// test Octree()
		Octree<Object> octree1 = new Octree<>();
		octree1.put(Long.MAX_VALUE, Long.MIN_VALUE, 0, null);
		assertThrows(IllegalArgumentException.class,
				() -> octree1.put(((double) Long.MAX_VALUE) * 2, Long.MIN_VALUE, 0, null));

		// test Octree(min, max)
		assertThrows(IllegalArgumentException.class, () -> new Octree<>(1, -1));
		Octree<Object> octree2 = new Octree<>(0, 1);
		octree2.put(1, 0, 1, null);
		assertThrows(IllegalArgumentException.class, () -> octree2.put(1, 1.1, 0, null));

		// test Octree(xMin, xMax, yMin, yMax, zMin, zMax)
		assertThrows(IllegalArgumentException.class, () -> new Octree<>(0, 0, 1, 5, 1, -1));
		Octree<Object> octree3 = new Octree<>(0, 1, 2, 3, 4, 5);
		octree3.put(0, 3, 4, null);
		assertThrows(IllegalArgumentException.class, () -> octree3.put(1, 1, 1, null));

		// test Octree(map)
		Octree<Object> octree4 = new Octree<>(map);
		assertEquals(octree4.get(0, 0, 0), "1");
		assertEquals(octree4.get(1, 0, 0), "2");
		assertEquals(octree4.get(0, 1, 0), "3");
		assertEquals(octree4.get(0, 0, 1), "4");
		assertEquals(octree4.size(), 4);

		// test Octree(distFunction)
		Octree<Object> octree5 = new Octree<>(distFunction);
		octree5.put(2, 0, 0, 17);
		octree5.put(1, 1, 1, 57);
		assertEquals(octree5.getNearestEntry(0, 0, 0).getValue(), 17);

		// test Octree(map, distFunction)
		Octree<Object> octree6 = new Octree<>(map, distFunction);
		assertEquals(octree6.get(0, 0, 0), "1");
		assertEquals(octree6.get(1, 0, 0), "2");
		assertEquals(octree6.get(0, 1, 0), "3");
		assertEquals(octree6.get(0, 0, 1), "4");
		assertEquals(octree6.size(), 4);
		octree6.clear();
		octree6.put(2, 0, 0, 12);
		octree6.put(1, 1, 1, 52);
		assertEquals(octree6.getNearestEntry(0, 0, 0).getValue(), 12);
	}

	/**
	 * Tests isEmpty(), size(), and clear()
	 */
	@Test
	void testSize() {
		Octree<Object> octree = new Octree<>();
		assertTrue(octree.isEmpty());
		assertEquals(octree.size(), 0);
		int count = 0;
		for (int i = 0; i < NUM_ENTRIES; i++) {
			if (octree.put(new Point3D(random.nextLong(), random.nextLong(), random.nextLong()),
					new Object()) == null)
				count++;
			assertEquals(count, octree.size());
		}
		assertFalse(octree.isEmpty());
		octree.clear();
		assertTrue(octree.isEmpty());
	}

	/**
	 * Tests all EntrySet methods, excluding those for which the default AbstractSet implementation
	 * is used.
	 */
	@SuppressWarnings("unlikely-arg-type")
	@Test
	void testEntrySet() {
		Octree<Integer> octree = new Octree<>(-1, 1);
		Set<Map.Entry<Point3D, Integer>> entryset = octree.entrySet();
		octree.put(1, 1, 1, 1);
		octree.put(0, 0, 0, 0);
		octree.put(-1, 0, 1, -2);
		octree.put(0.1, 0.1, 0.1, 17);

		// test size()
		assertEquals(entryset.size(), 4);

		// verify that add() is not supported
		assertThrows(UnsupportedOperationException.class, () -> entryset
				.add(new AbstractMap.SimpleImmutableEntry<Point3D, Integer>(null, null)));

		// test contains()
		assertTrue(entryset.contains(new AbstractMap.SimpleEntry<>(new Point3D(1, 1, 1), 1)));
		assertFalse(entryset.contains(new AbstractMap.SimpleEntry<>(new Point3D(1, 1, 1), 2)));
		assertEquals(octree.get(1, 1, 1), 1);

		// verify that iterator visits all elements and update the values of all map entries
		int count = 0;
		for (Map.Entry<Point3D, Integer> next : entryset) {
			count++;
			next.setValue(next.getValue() + 1);
		}
		assertEquals(count, 4);

		// verify that values were updated
		assertFalse(entryset.contains(new AbstractMap.SimpleEntry<>(new Point3D(1, 1, 1), 1)));
		assertTrue(entryset.contains(new AbstractMap.SimpleEntry<>(new Point3D(1, 1, 1), 2)));
		assertEquals(octree.get(1, 1, 1), 2);

		// test remove()
		entryset.remove(new AbstractMap.SimpleImmutableEntry<>(new Point3D(1, 1, 1), 2));
		assertEquals(entryset.size(), 3);
		assertEquals(octree.size(), 3);
		assertFalse(octree.containsKey(1, 1, 1));

		// test iterator() and iterator's hasNext(), next() and remove() methods
		Iterator<Map.Entry<Point3D, Integer>> iter = entryset.iterator();
		while (iter.hasNext()) {
			iter.next();
			iter.remove();
		}
		assertTrue(entryset.isEmpty());
		assertTrue(octree.isEmpty());

		// verify that iterator's remove() and next() methods throw exceptions correctly
		assertThrows(IllegalStateException.class, () -> iter.remove());
		assertThrows(NoSuchElementException.class, () -> iter.next());

		// verify that contains() throws exceptions correctly
		assertThrows(NullPointerException.class, () -> entryset.contains(null));
		assertThrows(NullPointerException.class, () -> entryset
				.contains(new AbstractMap.SimpleImmutableEntry<Point3D, Integer>(null, null)));
		assertThrows(ClassCastException.class, () -> entryset.contains(new Point3D(1, 1, 1)));
		assertThrows(ClassCastException.class, () -> entryset
				.contains(new AbstractMap.SimpleImmutableEntry<String, String>("hello", "world")));

		// test size(), clear(), and isEmpty()
		octree.put(1, 1, 1, 1);
		octree.put(0, 0, 0, 0);
		octree.put(-1, 0, 1, -2);
		assertEquals(entryset.size(), 3);
		entryset.clear();
		assertTrue(entryset.isEmpty());
		assertTrue(octree.isEmpty());

		// test remove()
		assertFalse(entryset
				.remove(new AbstractMap.SimpleEntry<Point3D, Integer>(new Point3D(1, 1, 1), 1)));
		octree.put(1, 1, 1, 1);
		assertTrue(entryset
				.remove(new AbstractMap.SimpleEntry<Point3D, Integer>(new Point3D(1, 1, 1), 1)));
		assertFalse(entryset
				.remove(new AbstractMap.SimpleEntry<Point3D, Integer>(new Point3D(1, 1, 1), 1)));
	}

	/**
	 * Tests the put(), get(), containsKey(), containsValue(), and remove() methods
	 */
	@Test
	void testPutGetContainsRemove() {
		// create a set of random points
		Set<Point3D> points = new HashSet<>(NUM_ENTRIES);
		for (int i = 0; i < NUM_ENTRIES; i++) {
			points.add(new Point3D(random.nextLong(), random.nextLong(), random.nextLong()));
		}
		Octree<Point3D> octree = new Octree<>();
		
		// test containsKey(), remove(), and get() on an empty Octree
		assertFalse(octree.containsKey(0, 0, 0));
		assertNull(octree.remove(0, 0, 0));
		assertNull(octree.get(0, 0, 0));
		
		// test containsKey(), remove(), and get() on a key that is not present in a non-empty Octree
		octree.put(1, 1, 1, null);
		assertFalse(octree.containsKey(0, 0, 0));
		assertNull(octree.remove(0, 0, 0));
		assertNull(octree.get(0, 0, 0));
		octree.clear();
		int count = 0;
		
		// test put() and containsValue() with Point3D argument
		for (Point3D point : points) {
			octree.put(point, point);
			assertEquals(++count, octree.size());

			// containsValue() is an O(n) operation, so it is not tested for every point to reduce
			// runtime of test
			if ((count % 1000) == 0)
				assertTrue(octree.containsValue(point));
		}
		
		// test containsKey() and get() with Point3D argument
		for (Point3D point : points) {
			assertTrue(octree.containsKey(point));
			assertEquals(point, octree.get(point));
		}
		
		// test containsKey() for an out-of-bounds value
		assertFalse(octree.containsKey(0, 0, Double.MAX_VALUE));
		
		// test remove() with Point3D argument
		for (Point3D point : points) {
			octree.remove(point);
			assertEquals(--count, octree.size());
		}
		
		// test put() with double arguments
		assertEquals(octree.size(), 0);
		for (Point3D point : points) {
			octree.put(point.getX(), point.getY(), point.getZ(), point);
			assertEquals(++count, octree.size());
		}
		
		// test containsKey() and get() with double arguments
		for (Point3D point : points) {
			assertTrue(octree.containsKey(point.getX(), point.getY(), point.getZ()));
			assertEquals(point, octree.get(point.getX(), point.getY(), point.getZ()));
		}
		
		// test remove() with double arguments
		for (Point3D point : points) {
			octree.remove(point.getX(), point.getY(), point.getZ());
			assertEquals(--count, octree.size());
		}
		assertEquals(octree.size(), 0);

		// verify that null values are allowed
		octree.put(0, 0, 0, null);
		assertEquals(octree.get(0, 0, 0), null);
		assertTrue(octree.containsKey(0, 0, 0));

		// verify that invalid inputs throw exceptions
		assertThrows(NullPointerException.class, () -> octree.put(null, new Point3D(0, 0, 0)));
		assertThrows(IllegalArgumentException.class,
				() -> octree.put(new Point3D(0, 0, Double.MAX_VALUE), null));
		assertThrows(NullPointerException.class, () -> octree.get(null));
		assertThrows(ClassCastException.class, () -> octree.get(new Object()));
		assertThrows(NullPointerException.class, () -> octree.containsKey(null));
		assertThrows(ClassCastException.class, () -> octree.containsKey(new Object()));
		assertThrows(NullPointerException.class, () -> octree.remove(null));
		assertThrows(ClassCastException.class, () -> octree.remove(new Object()));

		// verify that containsKey() works properly when searching for a value at the center of a
		// node
		Octree<Object> octree2 = new Octree<>(-1, 1);
		octree2.put(-1, -1, -1, null);
		octree2.put(1, 1, 1, null);
		assertFalse(octree2.containsKey(0, 0, 0));
		assertNull(octree2.remove(0, 0, 0));
		octree2.clear();
		octree2.put(0, 0, 0, null);
		octree2.put(1, 1, 1, null);
		assertTrue(octree2.containsKey(0, 0, 0));
	}

	/**
	 * Tests getNearestKey(), getNearestEntry(), getNearestKeys(), and GetNearestEntries().
	 */
	@Test
	void testGetNearestEntries() {
		Octree<Integer> octree = new Octree<>(0, 10);
		
		// test with empty Octree
		assertNull(octree.getNearestKey(0, 0, 0));
		assertNull(octree.getNearestEntry(0, 0, 0));
		assertTrue(octree.getNearestKeys(0, 0, 0, 10).isEmpty());
		assertTrue(octree.getNearestEntries(0, 0, 0, 10).isEmpty());
		
		// test with non-empty Octree
		octree.put(0, 0, 0, 0);
		octree.put(1, 0, 0, 1);
		octree.put(0, 1, 1, 2);
		octree.put(0, 1.5, 0, 3);
		octree.put(1, 1, 1, 4);
		octree.put(0, 2, 0, 5);
		octree.put(2, 0, 1, 6);
		assertEquals(octree.getNearestKey(new Point3D(0, 0, 0)), new Point3D(0, 0, 0));
		assertEquals(octree.getNearestEntry(new Point3D(0, 0, 0)),
				new AbstractMap.SimpleEntry<>(new Point3D(0, 0, 0), 0));
		
		// test getNearestKeys() and getNearestEntries() with n < octree.size()
		List<Point3D> keys = octree.getNearestKeys(new Point3D(0, 0, 0), 5);
		List<Map.Entry<Point3D, Integer>> entries = octree.getNearestEntries(new Point3D(0, 0, 0),
				5);
		assertEquals(keys.size(), 5);
		assertEquals(entries.size(), 5);
		for (int i = 0; i < 5; i++) {
			assertEquals(i, entries.get(i).getValue());
			assertEquals(keys.get(i), entries.get(i).getKey());
		}
		
		// test getNearestKeys() and getNearestEntries() with n > octree.size()
		keys = octree.getNearestKeys(new Point3D(0, 0, 0), 50);
		entries = octree.getNearestEntries(new Point3D(0, 0, 0), 50);
		assertEquals(keys.size(), 7);
		assertEquals(entries.size(), 7);
		for (int i = 0; i < 7; i++) {
			assertEquals(i, entries.get(i).getValue());
			assertEquals(keys.get(i), entries.get(i).getKey());
		}
		
		// other tests to ensure code coverage
		octree.clear();
		octree.put(2, 2, 2, 2);
		octree.put(9, 9, 9, 9);
		assertEquals(octree.getNearestEntry(5, 5, 5),
				new AbstractMap.SimpleImmutableEntry<>(new Point3D(2, 2, 2), 2));
		assertEquals(octree.getNearestEntry(8, 8, 8),
				new AbstractMap.SimpleEntry<>(new Point3D(9, 9, 9), 9));
		octree.clear();
		octree.put(5, 5, 5, 5);
		octree.put(10, 5.1, 5.1, 10);
		assertEquals(octree.getNearestEntry(10, 4.9, 4.9).getValue(), 10);
		
		// verify that using null points results in NullPointerException
		assertThrows(NullPointerException.class, () -> octree.getNearestKey(null));
		assertThrows(NullPointerException.class, () -> octree.getNearestEntry(null));
		assertThrows(NullPointerException.class, () -> octree.getNearestKeys(null, 5));
		assertThrows(NullPointerException.class, () -> octree.getNearestEntries(null, 5));
	}

	/**
	 * Tests the putAll() method.
	 */
	@Test
	void testPutAll() {
		Map<Point3D, Point3D> hashmap = new HashMap<>(NUM_ENTRIES);
		for (int i = 0; i < NUM_ENTRIES; i++) {
			Point3D point = new Point3D(random.nextLong(), random.nextLong(), random.nextLong());
			hashmap.put(point, point);
		}
		Octree<Point3D> octree = new Octree<>();
		octree.putAll(hashmap);
		assertEquals(octree.size(), hashmap.size());
		for (Point3D point : hashmap.keySet()) {
			assertEquals(point, octree.get(point));
		}
	}
}
