import numpy as np
from scipy import misc
from scipy import signal
import matplotlib.pyplot as plt

# Read face data
# -----------------------------------------------------------#
def read_face(person):
    faces = []
    for i in range(1, 11):

        file = str(i) + '.png'
        curr_face = misc.imread(file)
        faces.append(convt(curr_face).astype("float"))
    return faces

# convert a single face data to 1D array
# -----------------------------------------------------------#
def convt(face):
    height = np.shape(face)[0]
    width  = np.shape(face)[1]
    typed  = type(face[0,0])
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

    # Average face array
    avg_array = np.ndarray(shape=(size), dtype="float")

    # local mean face array after subtraction
    mean_array = np.ndarray(shape=(num_face, size), dtype="float")

    # Covariance Matrix array
    co_var = np.ndarray(shape=(size,size), dtype="float")

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
                mean_array[i,j] = face_data[pic][j]-avg_array[j]
        i += 1


    # Compute Covariance Matrix
    # ---------------------------------#
    trans = np.transpose(mean_array)
    co_var = np.dot(mean_array,trans)

    print("covariance",co_var)
    print(np.shape(co_var))


    # Compute the eigen vectors of A*A^T
    # ---------------------------------#
    junk, vector = np.linalg(co_var)
    vector = np.dot(trans, vector)
    print("eigen vector",vector)
    print(np.shape(vector))


    # Keep only K eigen vector
    # ---------------------------------#

# Main function#
# -----------------------------------------------------------#
faces = read_face(1)
eigen_face(faces)
num_face = 0