# mandytree
Declaration: all code and function is finished by ourself except part of print function assistant with gpt.

## 1: beginning of the program: 
1: running MandyTree.java

2: load file(should put data in data folder) 

3: help command （can print the command that you can input）

##
## 2: function description 
### i: insert (MandyTree 445 line)
1: initial the tree root with first node (447-453)

2: judge whether we can using easy insert method(458-461)
    
    i:easyinsert(474-481): if next insert larger than pprevious and (lastinsertnode have next and next node first 
    smaller than new insert or not have next)

    ii: if can easyinsert using previous path, otherwise search path and matin into a stack

3: using insertAfterfindpath method(488-515) (stop untill no new split index node)

    i insert into leaf node(490)

    (125-160) if node value > 2*degree then split (145-160), deggree number left node, degree+1number in new node(right node)

    ii if have split then insert popupkey to index node(508)

    (307-322)  if number > 2*degree+1 split, middle index pop up, and remaining divided into two part

### ii: delete (MandyTree 522 line)

1: (536) delete leaf node

    i: (178-185) find the sibling node for borrowing or merge

    ii: (187-201) borrow a value from sibling node

    iii: (203-222) merge with sibling

2: (545) delete index node

    i: (350-395) find the sibling node for borrowing or merge
    
    ii: (362-380) borrow from sibling, parent index vakue change
    
    iii: (383-401) merge with sibling and parent node

### iii: search (MandyTree 564 line)

i: (573) find out the first aim leaf node

ii: (585) according to leaf node is connect to each other; search other value in the range







##
## 3: binarysearch function (in btreeUtil)
### binary search children
insert when finding path
### binary search
in a node find a excat value position (if no find return -1)
### binary search index
in a node find a excat value position (if no find return the insert point)



