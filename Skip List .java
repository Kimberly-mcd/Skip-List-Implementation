import java.util.*;
/*Initializes an empty skip list.
Creates the head node (dummy) with no value, sets maxlevel and size to 0
*/
public class SkipListSet<T extends Comparable<T>> implements SortedSet<T> {
private static final double PROBABILITY = 0.5; // to generate level of each new
node
private final Node<T> head;
private int size;
private int maxLevel;
private final Random random = new Random();
public SkipListSet() {
this.head = new Node<>(null, 0);
this.size = 0;
this.maxLevel = 0;
}
/* Initializes the skip list and adds all elements from he collection to the list
Does not return anything, parameters include a collection that holds elements of
type T
*/
public SkipListSet(Collection<? extends T> collection) {
this();
if (collection == null) throw new NullPointerException();
addAll(collection);
}
private static class Node<T> {
T value;
List<Node<T>> next;
Node(T value, int level) {
this.value = value;
this.next = new ArrayList<>(Collections.nCopies(level + 1, null));
}
void adjustLevel(int newLevel) {
while (next.size() <= newLevel) {
next.add(null);
}
}
}
/* Inserts a new value into the skip list and finds the correct position for the
new node.
Determines a random level for the node, and adjusts the pointersl .
Returns true if the element was added, false if already in the list.
Parameter: Value - element to be inserted.
*/
@Override
public boolean add(T value) {
if (value == null) throw new NullPointerException();
Node<T> current = head;
List<Node<T>> update = new ArrayList<>(Collections.nCopies(maxLevel + 1,
null));
// Traverse top to bottom
for (int i = maxLevel; i >= 0; i--) {
while (current.next.get(i) != null &&
current.next.get(i).value.compareTo(value) < 0) {
current = current.next.get(i);
}
update.set(i, current); // Track where updates happen
}
current = current.next.get(0); // check lowest level
if (current != null && current.value.equals(value)) return false;
int level = generateRandomLevel();
if (level > maxLevel) {
// expand if new level is higher than max level
for (int i = maxLevel + 1; i <= level; i++) {
update.add(head);
}
head.adjustLevel(level);
maxLevel = level;
}
Node<T> newNode = new Node<>(value, level);
for (int i = 0; i <= level; i++) {
newNode.next.set(i, update.get(i).next.get(i));
update.get(i).next.set(i, newNode);
}
size++;
return true;
}
@Override
// Returns the last (aka largest) value in the list
public T last() {
if (isEmpty()) throw new NoSuchElementException();
Node<T> current = head;
for (int i = maxLevel; i >= 0; i--) {
while (current.next.get(i) != null) {
current = current.next.get(i);
}
}
return current.value;
}
@Override
// Returns the first value in the list
public T first() {
if (isEmpty()) throw new NoSuchElementException();
return head.next.get(0).value;
}
@Override
/* Adds elements from the collection into the list. Calls add(T value) for
each element.*/
public boolean addAll(Collection<? extends T> c) {
if (c == null) throw new NullPointerException();
boolean modified = false;
for (T value : c) {
modified |= add(value);
}
return modified;
}
@Override
/*Removes the specified value from the list.
Searches for the value, then adjusts the pointers.
Returns true if value is removed, false if element not found in the list
*/
public boolean remove(Object o) {
if (o == null) throw new NullPointerException();
@SuppressWarnings("unchecked")
T value = (T) o;
Node<T> current = head;
List<Node<T>> update = new ArrayList<>(Collections.nCopies(maxLevel + 1,
null));
for (int i = maxLevel; i >= 0; i--) {
while (current.next.get(i) != null &&
current.next.get(i).value.compareTo(value) < 0) {
current = current.next.get(i);
}
update.set(i, current);
}
current = current.next.get(0);
if (current == null || !current.value.equals(value)) return false;
for (int i = 0; i <= maxLevel && update.get(i).next.get(i) == current; i++)
{
update.get(i).next.set(i, current.next.get(i));
}
while (maxLevel > 0 && head.next.get(maxLevel) == null) {
maxLevel--;
}
size--;
return true;
}
@Override
public void clear() {
for (int i = 0; i <= maxLevel; i++) {
head.next.set(i, null);
}
size = 0;
maxLevel = 0;
}
@Override
public int size() {
return size;
}
@Override
public boolean isEmpty() {
return size == 0;
}
@Override
// scan skip list for value.
public boolean contains(Object o) {
if (o == null) throw new NullPointerException();
@SuppressWarnings("unchecked")
T value = (T) o;
Node<T> current = head;
for (int i = maxLevel; i >= 0; i--) {
while (current.next.get(i) != null &&
current.next.get(i).value.compareTo(value) < 0) {
current = current.next.get(i);
}
}
current = current.next.get(0);
return current != null && current.value.equals(value);
}
@Override
public boolean containsAll(Collection<?> c) {
if (c == null) throw new NullPointerException();
for (Object o : c) {
if (!contains(o)) {
return false;
}
}
return true;
}
@Override
// Removes all elements in the collection from the list.
public boolean removeAll(Collection<?> c) {
if (c == null) throw new NullPointerException();
boolean modified = false;
for (Object o : c) {
modified |= remove(o);
}
return modified;
}
// Rebalance list by clearing it and adding all the elements again
public void reBalance() {
List<T> values = new ArrayList<>();
for (T value : this) {
values.add(value);
}
clear(); // Clear the current skip list
for (T value : values) {
add(value); // Re-add all elements to re-randomize their heights
}
}
@Override
// Keeps only the elements in the list that are in the given colllection
public boolean retainAll(Collection<?> c) {
if (c == null) throw new NullPointerException();
boolean modified = false;
Iterator<T> it = iterator();
while (it.hasNext()) {
T value = it.next();
if (!c.contains(value)) {
it.remove();
modified = true;
}
}
return modified;
}
private int generateRandomLevel() {
int level = 0;
while (random.nextDouble() < PROBABILITY && level < maxLevel + 1) {
level++;
}
return level;
}
@Override
public Iterator<T> iterator() {
return new SkipListSetIterator();
}
private class SkipListSetIterator implements Iterator<T> {
private Node<T> current = head.next.get(0);
private Node<T> lastReturned = null;
@Override
public boolean hasNext() {
return current != null;
}
@Override
public T next() {
if (current == null) throw new NoSuchElementException();
lastReturned = current;
current = current.next.get(0);
return lastReturned.value;
}
@Override
public void remove() {
if (lastReturned == null) throw new IllegalStateException();
SkipListSet.this.remove(lastReturned.value);
lastReturned = null;
}
}
@Override
public SortedSet<T> headSet(T toElement) {
throw new UnsupportedOperationException();
}
@Override
public SortedSet<T> tailSet(T fromElement) {
throw new UnsupportedOperationException();
}
@Override
public SortedSet<T> subSet(T fromElement, T toElement) {
throw new UnsupportedOperationException();
}
@Override
public Comparator<? super T> comparator() {
return null;
}
@Override
public Object[] toArray() {
Object[] result = new Object[size];
int i = 0;
for (T value : this) result[i++] = value;
return result;
}
@Override
public <E> E[] toArray(E[] a) {
if (a.length < size) {
a = (E[])
java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
}
int i = 0;
Object[] result = a;
for (T value : this) result[i++] = value;
if (a.length > size) a[size] = null;
return a;
}
}
