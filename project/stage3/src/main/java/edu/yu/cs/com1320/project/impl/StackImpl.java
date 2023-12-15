package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Stack;

public class StackImpl<T> implements Stack<T> {
        private int size;

        private class StackNode {
            private T data;
            private StackNode next;

            StackNode(T data) {
                this.data = data;
                this.next = null;
            }

        }

        StackNode head = null;

        public StackImpl() {
            this.size = 0;
        }

        public void push(T data) {
            // what should happen if the data is null?
            StackNode nodeToAdd = new StackNode(data);
            if (head == null) {// if the stack is empty
                head = nodeToAdd;
            } else {// if the stack is not empty, set the first element to the next element
                StackNode oldHead = head;
                head = nodeToAdd;
                head.next = oldHead;
            }
            size++;
        }

        public T pop() {
            if (head == null) {
                return null;
            } else {
                T dataToReturn = head.data;
                head = head.next;// CHECK IF IT'S AN ISSUE IF HEAD.NEXT IS NULL
                size--;
                return dataToReturn;
            }
        }

        public T peek() {
            if (head == null) {
                return null;
            } else {
                return head.data;
            }
        }

        public int size() {
            return this.size;
        }
}
