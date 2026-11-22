import bcrypt from "bcrypt";
import jwt from "jsonwebtoken";
import { config } from "../../config.js";
import { findUserForLogin } from "../../repositories/user.repository.js";

export async function loginUser(email: string, password: string) {
    if (Buffer.byteLength(password, "utf8") > 72 || email.includes("\0")) {
        return null;
    }

    const user = await findUserForLogin(email);
    if (!user || !(await bcrypt.compare(password, user.password_hash))) {
        return null;
    }

    const token = jwt.sign({}, config.jwtSecret, {
        subject: String(user.id),
        expiresIn: "2h",
        algorithm: "HS256",
    });

    return {
        token,
        user: { id: user.id, full_name: user.full_name, email: user.email },
    };
}
