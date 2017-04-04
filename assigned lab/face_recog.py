import numpy as np
from scipy import misc
import random as rd
from scipy import signal
import matplotlib.pyplot as plt
import cv2
# Average face array
# avg_array = []
# largest_vector = []

#Face Recognition Function
# -----------------------------------------------------------#
def face_recog (input, avg_array, largest_vector, weight):
    # global avg_array
    # global largest_vector

    size = np.shape(input)[0]
    norm_array = np.ndarray(shape=(size), dtype="float64")
    project  = np.ndarray(shape=(1,size), dtype="float64")
    diff_sqr = np.ndarray(shape=(1,size), dtype="float64")

    # print(size)
    # print("input shape: ", np.shape(input))
    # print("avg shape: ", np.shape(avg_array))
    # print("norm shape: ", np.shape(norm_array))

    # Subtract input from average face
    # -----------------------------------------#
    norm_array = input-avg_array


    # Calculate projection on eigenface vector
    # -----------------------------------------#
    # print("vector: ", np.shape(largest_vector), "norm: ", np.shape(norm_array))
    project = np.dot(largest_vector, (np.atleast_2d(norm_array)).transpose() )
    # print("project: ", np.shape(project))
    distance =  weight-project
    abs = np.linalg.norm(distance, axis=0)
    # print("abs:", np.shape(abs))
    guess = np.argmin(abs)/(7)

    # print(guess)
    # print(abs)
    return guess
    # for i in range(7):
    #     curr_trans = (np.atleast_2d(largest_vector[i, :]))
    #     temp = np.dot(curr_trans, (np.atleast_2d(norm_array)).transpose())
    #     temp = np.dot(temp, np.atleast_2d(largest_vector[i, :]))
    #     project += temp


    # Compute covariance for next step
    # -----------------------------------------#


    # Calculating the difference square of normalize face and projection
    # -----------------------------------------#
    # for i in range(size):
    #     diff_sqr[0, i] = (project[0, i]-norm_array[i])**2
    #     # diff_sqr[0, i] += ((project[0, i] - norm_array[i]) ** 2)/
    # distance = np.amin(diff_sqr)

    # print(distance)
    # return distance
    # print("norm: ", np.shape(norm_array))
    # print("project: ", np.shape(project))



# Read 1 face data
# -----------------------------------------------------------#
def read_a_face(target, exclude):
    faces = []
    for i in range(1, 11):
        if(i not in exclude):
            if (target == 0):
                file = str(i) + '.png'
            if(target == 1):
                file = str(i) + ' (1).png'
            elif(target == 2):
                file = str(i) + ' (2).png'
            elif (target == 3):
                file = str(i) + ' (3).png'
            elif (target == 4):
                file = str(i) + ' (4).png'
            elif (target == 5):
                file = str(i) + ' (5).png'
            elif (target == 6):
                file = str(i) + ' (6).png'
            elif (target == 7):
                file = str(i) + ' (7).png'
            elif (target == 8):
                file = str(i) + ' (8).png'
            elif (target == 9):
                file = str(i) + ' (9).png'
            else:
                file = str(i) + '.png'
            curr_face = misc.imread(file)
            faces.append(convt(curr_face).astype("float"))
    return faces

    # faces = []
    # file = '7 (2).png'
    # curr_face = misc.imread(file)
    # faces.append(convt(curr_face).astype("float"))
    # return faces




# Read face data
# -----------------------------------------------------------#
def read_face(target):
    faces = []
    list = range(1,11)
    random = rd.sample(list,7)
    print(random)
    for i in range(1, 11):
        if(i in random):
            if (target == 0):
                file = str(i) + '.png'
            if(target == 1):
                file = str(i) + ' (1).png'
            elif(target == 2):
                file = str(i) + ' (2).png'
            elif (target == 3):
                file = str(i) + ' (3).png'
            elif (target == 4):
                file = str(i) + ' (4).png'
            elif (target == 5):
                file = str(i) + ' (5).png'
            elif (target == 6):
                file = str(i) + ' (6).png'
            elif (target == 7):
                file = str(i) + ' (7).png'
            elif (target == 8):
                file = str(i) + ' (8).png'
            elif (target == 9):
                file = str(i) + ' (9).png'
            else:
                file = str(i) + '.png'
            curr_face = misc.imread(file)
            faces.append(convt(curr_face).astype("float"))
    return faces, random

# convert a single face data to 1D array
# -----------------------------------------------------------#
def convt(face):
    height = np.shape(face)[0]
    width = np.shape(face)[1]
    typed = type(face[0,0])
    new_face = np.ndarray(shape=(height*width), dtype=typed)
    for y in range(height):
        for x in range(width):
            new_face[x+y*width] = face[y,x]
    return new_face

# Compute eignen faces
# -----------------------------------------------------------#
def eigen_face(face_data):
    num_face = np.shape(face_data)[0]
    size = np.shape(face_data)[1]
    # print(size)
    avg_array = np.ndarray(shape=(size), dtype="float64")

    # local mean face array after subtraction
    mean_array = np.ndarray(shape=(num_face, size), dtype="float")

    # Covariance Matrix array
    co_var = np.ndarray(shape=(num_face,num_face), dtype="float")

    # Compute average of face data
    # ---------------------------------#
    for pic in range(num_face):
        for i in range(size):
            avg_array[i] += float(face_data[pic][i])
    avg_array = avg_array/num_face
    # Subtract the mean face
    # ---------------------------------#
    i = 0
    for pic in range(num_face):
        for j in range(size):
            mean_array[i, j] = face_data[pic][j] - avg_array[j]
        i += 1
    mean_array = np.transpose(mean_array)

    # Compute Covariance Matrix
    # ---------------------------------#
    # print("mean shape: ",np.shape(mean_array))
    trans = np.transpose(mean_array)
    co_var = np.dot(trans, mean_array)
    co_var = co_var/num_face

    # print("covar: ", covar)
    # print("co_var: ", co_var)
    # print("co_var shape: ", np.shape(co_var))
    # print("covar shape: ", np.shape(covar))


    # Compute the eigen vectors of A*A^T
    # ---------------------------------#
    value, vector = np.linalg.eig(co_var)
    sort_index = value.argsort()[::-1]      #Create sorting index
    value = value[sort_index]               #Sort through eigenvalues
    vector = vector[sort_index]             #Sort through eigenvectors

    # print("label")
    # print(value)

    # Normalize Eigen vectors
    # ---------------------------------#
    vector = np.transpose(vector)
    vector = np.dot(mean_array, vector)
    mag = np.linalg.norm(vector)
    vector = vector/mag

    # Keep only K eigen vector
    # ---------------------------------#
    largest_vector = vector
    largest_vector = largest_vector.transpose()
    print("largest vector", np.shape(largest_vector) )
    print("mean_array", np.shape(mean_array))
    temp = np.dot(largest_vector, mean_array)
    return largest_vector, avg_array, temp

    # Reconstruction of face
    # ---------------------------------#
    restrt = np.ndarray(shape=(1, size), dtype="float")
    for j in range(7):
        for i in range(7):
            curr_trans = (np.atleast_2d(largest_vector[i, :]))
            temp = np.dot(curr_trans, (np.atleast_2d(mean_array[j])).transpose())
            # print(temp)
            temp = np.dot(temp, np.atleast_2d(largest_vector[i, :]))
            # print(temp)
            restrt += temp

    min = np.min(restrt)
    max = np.max(restrt)
    for i in range(size):
        restrt[0,i]= (restrt[0,i]-min)/(max-min)*255
    # print(np.amax(restrt), np.amin((restrt)))

    final = np.ndarray(shape=(112,92,3), dtype='uint8')
    for y in range(112):
        for x in range(92):
            final[y, x, 0] = restrt[0, y*92+x]
            final[y, x, 1] = restrt[0, y*92+x]
            final[y, x, 2] = restrt[0, y*92+x]

    plt.imshow(final, vmin=0, vmax=255)
    plt.show()
    return (largest_vector, avg_array)

# Main function#
# ------------------------------------------------------------------------#
test_face = []
train_face = []
random_array = []
avg= []
largest=[]
num_face = 10
train_size = 7
test_size = 3
new_train_face = np.ndarray(shape=(num_face*train_size, 10304 ), dtype="float")

# Read input faces
for i in range(num_face):
    (temp,random) = read_face(i)
    new_train_face[i * train_size:(i + 1) * train_size] = temp
    test_face.append(read_a_face(i,random))

(larg, avg, weight) = eigen_face(new_train_face)
total   = 0
correct = 0
incorrect = 0
for i in range(num_face):
    for j in range(test_size):
        total += 1
        out = face_recog(test_face[i][j],avg,larg,weight)
        print("Input face ID: ",i, " >>>>>> guessed ID: ",out)
        if(out == i):
            correct +=1
        else:
            incorrect+=1

print("total faces: ", total)
print("Sucess rate: ", correct/float(total)*100.0, "%")
print("Fail rate: ", incorrect/float(total)*100.0, "%")
# Combine all face into 1 2D array
# for i in range(4):
#     (large, average) = eigen_face(train_face[i])
#     largest.append(large)
#     avg.append(average)


# thresh = 3.0*10**(-5)
# temp = []
# min_cost = 999999.99
# index = 0
# verify=0
#
# # Loop through different test people
# for k in range(4):
#     # Loop through photos of each test people
#     for j in range(10):
#         total+=1
#         del temp[:]
#         # Check against all data bases
#         for i in range(4):
#             temp.append(face_recog(test_face[k][j], avg[i], largest[i]))
#
#
#         verify =0       #Indicate whether we have any face lower than thresh
#         verify_ary =[]  #Inidcate which face is lower than thresh
#
#         # Verify each whether each distance is within the threshold
#         for i in range(4):
#             if(temp[i]<= thresh):
#                 verify=1
#                 verify_ary.append(i)
#
#         # if all distance is above threshold and current face is not in database
#         if(verify==0 and k>=4):
#             correct+=1
#
#         #if all distance is above threshold and current face is in database
#         elif(verify==0 and k < 4):
#             incorrect+=1
#
#         #if some distance is within threshold
#         if(verify >0):
#             # correct+=1
#             min_cost = 9999999.99
#             for i in range(4):
#                 if(i in verify_ary) and (temp[i]<min_cost):
#                     min_cost = temp[i]
#                     index = i
#             print("person: ", index,"distance: ", min_cost)
#             if(index == k):
#                 correct+=1
#
#             else:
#                 incorrect += 1
#         del verify_ary[:]
#
#
# #Pick the minimum distance out of all
# print("total faces: ", total)
# print("Sucess rate: ", correct/float(total)*100.0, "%")
# print("Fail rate: ", incorrect/float(total)*100.0, "%")