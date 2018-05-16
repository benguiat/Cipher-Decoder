## Metropolis-Hastings Cipher Decoder

Roughly following a paper on cipher decoding by Fan & Wen (2016), this is an implementation of a substitution cipher decoder using the Metropolis-Hastings algorithm. The implementation can be found in the file “mcmc_decoder.java” and runs with Java 8. It requires two inputs to run: an encrypted text of 26 alphabetic letters and spaces, and a reference text from which to draw a bigram letter frequency distribution from.
<p>
A substitution cipher can be solved given a key that tells you which letters have been substituted in the cipher with another letter. For example, ‘A’ in the plaintext has been replaced with ‘X’ in the cipher, ‘B’ with ‘E’, etc. 
<p>
  
## Metropolis Hasting Algorithm
Markov Chain Monte Carlo (MCMC) methods attempt to ‘guess’ what this key is by generating a random one, making small changes, and then comparing the outputs by scoring them based on a probability distribution. Metropolis-Hasting, specifically, works in the following way:<br>
* <b>1.</b> Initialize by generating a random key.<br>
* <b>2.</b> Calculate a score (log-likelihood) for this key by adding together the transition probabilities
between letters of the text decoded by the randomly generated key (for very tiny and small
numerical reasons, this is all done in log space).<br>
* <b>3.</b> Swap two letters of your random key and calculate a score for this new key.<br>
* <b>4.</b> Calculate an acceptance value based on your key scores, X being previous key and Y being
new key:<br>
```
  A(X, Y) = (∏Y/∏X)^p
  ```
* <b>5.</b> If this acceptance value is greater than a random sample from a uniform distribution,
proceed with the newer key (otherwise reject it) and repeat steps 2-5 for the number of specified iterations.<br>
