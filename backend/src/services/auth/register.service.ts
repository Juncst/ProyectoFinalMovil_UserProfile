import bcrypt from "bcrypt";
import { createUser, findUserByEmail } from "../../repositories/user.repository.js";

export async function registerUser(fullName: string, email: string, password: string) {
    if (await findUserByEmail(email)) {
        return null;
    }

    const passwordHash = await bcrypt.hash(password, 12);
    return createUser(fullName, email, passwordHash);
}
