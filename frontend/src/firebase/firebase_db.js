import {
	collection,
	doc,
	getDoc,
	getDocs,
	addDoc,
	updateDoc,
	deleteDoc,
	query,
	where,
	orderBy,
	limit,
	setDoc,
} from "firebase/firestore";

import { FirestoreDB } from "./firebase_config";

class FirebaseFirestore {
	// google login add user to firestore
	googleLogin = async function (userID, userDisplayName, userEmail) {
		const docRef = doc(FirestoreDB, "users", userID);
		const docSnap = await getDoc(docRef);
		if (docSnap.exists()) {
			console.log("Document data:", docSnap.data());
		} else {
			// doc.data() will be undefined in this case
			console.log("No such document!");
		}
	};

	// register user to firestore
	register = async function (userID, userDisplayName, userEmail) {
		await setDoc(doc(FirestoreDB, "users", userID), {
			name: userDisplayName,
			email: userEmail,
		});
		console.log("created user in firestore");
	};
}

const FBInstanceFirestore = new FirebaseFirestore();
export default FBInstanceFirestore;
