import re


# check whether a is able to be divided by b with no remainder
# return True or False
# note: if divisor is 0, return False
def is_divisible(a, b):
    if b != 0:
        if a % b == 0:
            return True
        else:
            return False
    else:
        return False


# remove the element n in list l
# if n is not in l, return l
def remove_element(l, n):
    if n not in l:
        return l
    else:
        list = []
        for i in l:
            if i != n:
                list.append(i)
        return list


# check whether number list l1 and number list l2 are exactly same.(Same size, Same Content)
# return True or False
# note: l1 & l2 may be multi-dimensional
def is_equal_list(l1, l2):
    judge = False
    if len(l1) == len(l2):
        if type(l1[0]) == list:
            for i in range(len(l1)):
                for k in range(len(l1[0])):
                    if l1[i][k] != l2[i][k]:
                        return False
            return True
        else:
            for i in range(len(l1)):
                if l1[i] != l2[i]:
                    return False
            return True
    else:
        return False


# process the matrix m in required operation d.
# direction 1: Vertical Flip
# direction 2: Horizontal Flip
# direction 3: Transpose
# return processed matrix
# note: m may be two-dimensional
def matrix_process(m, d):
    v = len(m)
    h = len(m[0])
    if d == 1:
        for i in range(h):
            for j in range(int(v / 2)):
                tem = m[v - j - 1][i]
                m[v - j - 1][i] = m[j][i]
                m[j][i] = tem
                j += 1
            i += 1
    elif d == 2:
        for i in range(v):
            for j in range(int(h / 2)):
                tem = m[i][v - j - 1]
                m[i][v - j - 1] = m[i][j]
                m[i][j] = tem
                j += 1
            i += 1
    elif d == 3:
        # n = [[] * v for i in range(h)]
        # print(n)
        # for i in range(h):
        #     for j in range(v):
        #         n[i][j] = m[j][i]
        #         # print(n[i][j])
        #         j += 1
        #         # print(j)
        # i += 1
        # # print(i)
        # m = n
        matrix = [[row[i] for row in m] for i in range(h)]
        # print(matrix)
        m = matrix
    return m


# read a sentence from given filename, then reverse the order of word and punctuation.
# return reversed sentence
# note1: punctuations still follows the word it follows originally
#     example: "Hello World!" -> "!World Hello"
#                     ^^^^^^      ^^^^^^
# note2: abbreviation with punctuations like "can't" also should be reversed to "t'can"
# possible APIs: str.split(str) str.isalpha() str.join(sequence)
def reverse_sentence_from_file(filename):
    f = open(filename, 'r+')
    str = ''.join(reversed(re.split(r'(\W)', f.readline())))
    # content = f.readline().split()
    # list = []
    # for con in content:
    #     if con.isalpha():
    #         list.append(con)
    #     else:
    #         for n in con:
    #             if not n.isalpha():
    #                 if n == "'":
    #                     temp = con.split("'")
    #                     temp.reverse()
    #                     string = "'".join(temp)
    #                     list.append(string)
    #                     break
    #                 else:
    #                     # for c in range(i):
    #                     #     tem = con[i - c]
    #                     #     con[i - c] = con[i - c - 1]
    #                     #     con[i - c - 1] = tem
    #                     #     c += 1
    #                     string = con[-1] + con[0:(len(con)-1)]
    #                     list.append(string)
    #                     break
    # list.reverse()
    # str = ' '.join(list)
    return str


