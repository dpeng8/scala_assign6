package forcomp

import common._

object Anagrams {

  /** A word is simply a `String`. */
  type Word = String

  /** A sentence is a `List` of words. */
  type Sentence = List[Word]

  /** `Occurrences` is a `List` of pairs of characters and positive integers saying
   *  how often the character appears.
   *  This list is sorted alphabetically w.r.t. to the character in each pair.
   *  All characters in the occurrence list are lowercase.
   *
   *  Any list of pairs of lowercase characters and their frequency which is not sorted
   *  is **not** an occurrence list.
   *
   *  Note: If the frequency of some character is zero, then that character should not be
   *  in the list.
   */
  type Occurrences = List[(Char, Int)]

  /** The dictionary is simply a sequence of words.
   *  It is predefined and obtained as a sequence using the utility method `loadDictionary`.
   */
  val dictionary: List[Word] = loadDictionary

  /** Converts the word into its character occurrence list.
   *
   *  Note: the uppercase and lowercase version of the character are treated as the
   *  same character, and are represented as a lowercase character in the occurrence list.
   *
   *  Note: you must use `groupBy` to implement this method!
   */
  def wordOccurrences(w: Word): Occurrences = {
    w.toLowerCase().groupBy((c: Char) => c).mapValues(_.size).toList.sortWith(_._1 < _._1)
  }

  /** Converts a sentence into its character occurrence list. */
  def sentenceOccurrences(s: Sentence): Occurrences = {
    def helper(s: Sentence, acc: Occurrences): Occurrences = {
      if(s.isEmpty){
        acc
      }
      else{
        (wordOccurrences(s.head) ::: acc ::: sentenceOccurrences(s.tail)).groupBy((pair: (Char,Int)) => pair._1).mapValues((p : List[(Char,Int)]) => p.foldLeft(0){(z,i) => z + i._2}).toList
      }
    }
    helper(s, List[(Char, Int)]()).sortWith(_._1 < _._1)
  }

  /** The `dictionaryByOccurrences` is a `Map` from different occurrences to a sequence of all
   *  the words that have that occurrence count.
   *  This map serves as an easy way to obtain all the anagrams of a word given its occurrence list.
   *
   *  For example, the word "eat" has the following character occurrence list:
   *
   *     `List(('a', 1), ('e', 1), ('t', 1))`
   *
   *  Incidentally, so do the words "ate" and "tea".
   *
   *  This means that the `dictionaryByOccurrences` map will contain an entry:
   *
   *    List(('a', 1), ('e', 1), ('t', 1)) -> Seq("ate", "eat", "tea")
   *
   */
  lazy val dictionaryByOccurrences: Map[Occurrences, List[Word]] = {
    def helper(d: List[Word], acc: Map[Occurrences, List[Word]]): Map[Occurrences, List[Word]] = {
      if(d.isEmpty){
        acc
      }
      else{
        val occur = wordOccurrences(d.head)
        if(acc.contains(occur)){
          helper(d.tail, acc + (occur -> (d.head :: acc(occur))))
        }
        else{
          helper(d.tail, acc + (occur -> List(d.head)))
        }
      }
    }
    helper(dictionary, Map[Occurrences, List[Word]]())
  }

  /** Returns all the anagrams of a given word. */
  def wordAnagrams(word: Word): List[Word] = {
    dictionaryByOccurrences(wordOccurrences(word))
  }

  /** Returns the list of all subsets of the occurrence list.
   *  This includes the occurrence itself, i.e. `List(('k', 1), ('o', 1))`
   *  is a subset of `List(('k', 1), ('o', 1))`.
   *  It also include the empty subset `List()`.
   *
   *  Example: the subsets of the occurrence list `List(('a', 2), ('b', 2))` are:
   *
   *    List(
   *      List(),
   *      List(('a', 1)),
   *      List(('a', 2)),
   *      List(('b', 1)),
   *      List(('a', 1), ('b', 1)),
   *      List(('a', 2), ('b', 1)),
   *      List(('b', 2)),
   *      List(('a', 1), ('b', 2)),
   *      List(('a', 2), ('b', 2))
   *    )
   *
   *  Note that the order of the occurrence list subsets does not matter -- the subsets
   *  in the example above could have been displayed in some other order.
   */
  def combinations(occurrences: Occurrences): List[Occurrences] = {
    def helper(occurrences: Occurrences, list: List[Occurrences]): List[Occurrences] = {
      if(occurrences.isEmpty){
        Nil :: list
      }
      else{
        val sublist = construct_sub(occurrences.head._1, occurrences.head._2, List[(Char, Int)]())
        if (occurrences.tail.isEmpty){
          for(x <- sublist) yield List(x)
        }
        else{
          val old_list = helper(occurrences.tail, List())
          for(x <- sublist;y <- old_list) yield x :: y
        }
      }
    }
    def construct_sub(char: Char, x: Int, acc: Occurrences): Occurrences = {
      if(x == -1){
        acc
      }
      else {
        construct_sub(char, x - 1, (char, x) :: acc)
      }
    }
    helper(occurrences,List[Occurrences]()).map(x => x.filter(y => y._2 > 0)).map(x => x.sortWith(_._1 < _._1))
  }

  /** Subtracts occurrence list `y` from occurrence list `x`.
   *
   *  The precondition is that the occurrence list `y` is a subset of
   *  the occurrence list `x` -- any character appearing in `y` must
   *  appear in `x`, and its frequency in `y` must be smaller or equal
   *  than its frequency in `x`.
   *
   *  Note: the resulting value is an occurrence - meaning it is sorted
   *  and has no zero-entries.
   */
  def subtract(x: Occurrences, y: Occurrences): Occurrences = {
    val ymap = y.map(t => t._1 -> t._2).toMap withDefaultValue 0
    val xmap = x.map(t => t._1 -> t._2).toMap
    def sub(term: (Char,Int)): (Char,Int) = {
      (term._1, term._2 - ymap(term._1))
    }
    xmap.map(sub).toList.filter(x => x._2 > 0)
  }

  /** Returns a list of all anagram sentences of the given sentence.
   *
   *  An anagram of a sentence is formed by taking the occurrences of all the characters of
   *  all the words in the sentence, and producing all possible combinations of words with those characters,
   *  such that the words have to be from the dictionary.
   *
   *  The number of words in the sentence and its anagrams does not have to correspond.
   *  For example, the sentence `List("I", "love", "you")` is an anagram of the sentence `List("You", "olive")`.
   *
   *  Also, two sentences with the same words but in a different order are considered two different anagrams.
   *  For example, sentences `List("You", "olive")` and `List("olive", "you")` are different anagrams of
   *  `List("I", "love", "you")`.
   *
   *  Here is a full example of a sentence `List("Yes", "man")` and its anagrams for our dictionary:
   *
   *    List(
   *      List(en, as, my),
   *      List(en, my, as),
   *      List(man, yes),
   *      List(men, say),
   *      List(as, en, my),
   *      List(as, my, en),
   *      List(sane, my),
   *      List(Sean, my),
   *      List(my, en, as),
   *      List(my, as, en),
   *      List(my, sane),
   *      List(my, Sean),
   *      List(say, men),
   *      List(yes, man)
   *    )
   *
   *  The different sentences do not have to be output in the order shown above - any order is fine as long as
   *  all the anagrams are there. Every returned word has to exist in the dictionary.
   *
   *  Note: in case that the words of the sentence are in the dictionary, then the sentence is the anagram of itself,
   *  so it has to be returned in this list.
   *
   *  Note: There is only one anagram of an empty sentence.
   */
  def sentenceAnagrams(sentence: Sentence): List[Sentence] = {
    def helper(occurrences: Occurrences, acc: List[Sentence]): List[Sentence] = {
      if(occurrences.isEmpty){
        acc
      }
      else{
        val map = dictionaryByOccurrences withDefaultValue Nil
        val list = for(x <- combinations(occurrences);word <- map(x))
          yield helper(subtract(occurrences,x), acc.map(y => word :: y))
        print(list)
        list.foldLeft(List[Sentence]())((z,i) => i ::: z)
      }
    }
    val occurrences = sentenceOccurrences(sentence)
    if(occurrences.isEmpty) {
      List[Word]() :: List[Sentence]()
    }
    else{
      helper(occurrences, List[Word]() :: List[Sentence]())
    }
  }
}
